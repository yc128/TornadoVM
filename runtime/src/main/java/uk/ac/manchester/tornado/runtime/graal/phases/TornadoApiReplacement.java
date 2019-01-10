/*
 * Copyright (c) 2018, 2019, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * Copyright (c) 2009, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Authors: James Clarkson
 *
 */
package uk.ac.manchester.tornado.runtime.graal.phases;

import static uk.ac.manchester.tornado.runtime.common.Tornado.TORNADO_LOOPS_REVERSE;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.vm.ci.meta.LocalAnnotation;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.graph.iterators.NodeIterable;
import org.graalvm.compiler.loop.InductionVariable;
import org.graalvm.compiler.loop.LoopEx;
import org.graalvm.compiler.loop.LoopsData;
import org.graalvm.compiler.nodes.*;
import org.graalvm.compiler.nodes.calc.IntegerLessThanNode;
import org.graalvm.compiler.nodes.java.StoreIndexedNode;
import org.graalvm.compiler.phases.BasePhase;

import uk.ac.manchester.tornado.api.type.annotations.Atomic;
import uk.ac.manchester.tornado.runtime.common.Tornado;
import uk.ac.manchester.tornado.runtime.graal.nodes.AtomicAccessNode;
import uk.ac.manchester.tornado.runtime.graal.nodes.ParallelOffsetNode;
import uk.ac.manchester.tornado.runtime.graal.nodes.ParallelRangeNode;
import uk.ac.manchester.tornado.runtime.graal.nodes.ParallelStrideNode;

public class TornadoApiReplacement extends BasePhase<TornadoSketchTierContext> {

    @Override
    protected void run(StructuredGraph graph, TornadoSketchTierContext context) {
        replaceParameterAnnotations(graph, context);
        replaceLocalAnnotations(graph, context);
    }

    private void replaceParameterAnnotations(StructuredGraph graph, TornadoSketchTierContext context) {
        final Annotation[][] parameterAnnotations = graph.method().getParameterAnnotations();

        for (int i = 0; i < parameterAnnotations.length; i++) {

            for (Annotation an : parameterAnnotations[i]) {
                // System.out.printf("annotation: param[%d]: %s\n",i,an);
                if (an instanceof Atomic) {
                    final ParameterNode param = graph.getParameter(i);
                    final AtomicAccessNode atomicAccess = graph.addOrUnique(new AtomicAccessNode(param));
                    // param.replaceAtMatchingUsages(atomicAccess, usage ->
                    // usage instanceof StoreIndexedNode);

                    // Partial solution to create an atomic node.
                    // TODO: replace for an ATOMIC_ADD node etc, depending on
                    // the operation.
                    NodeIterable<Node> usages = param.usages();
                    for (Node n : usages) {
                        if (n instanceof ValuePhiNode) {
                            param.replaceAtMatchingUsages(atomicAccess, usage -> usage instanceof ValuePhiNode);
                            break;
                        } else if (n instanceof StoreIndexedNode) {
                            param.replaceAtMatchingUsages(atomicAccess, usage -> usage instanceof StoreIndexedNode);
                            break;
                        }
                    }

                }
            }
        }

    }

    private void replaceLocalAnnotations(StructuredGraph graph, TornadoSketchTierContext context) {

        // build node -> annotation mapping
        Map<ResolvedJavaMethod, LocalAnnotation[]> methodToAnnotations = new HashMap<>();

        methodToAnnotations.put(context.getMethod(), context.getMethod().getLocalAnnotations());

        for (ResolvedJavaMethod inlinee : graph.getMethods()) {

            if (inlinee.getLocalAnnotations().length > 0) {
                methodToAnnotations.put(inlinee, inlinee.getLocalAnnotations());
            }
        }

        Map<Node, LocalAnnotation> parallelNodes = new HashMap<>();

        graph.getNodes().filter(FrameState.class).forEach((fs) -> {
            // Tornado.trace("framestate: method=%s,",fs.method().getName());
            if (methodToAnnotations.containsKey(fs.getMethod())) {
                for (LocalAnnotation an : methodToAnnotations.get(fs.getMethod())) {
                    if (fs.bci >= an.getStart() && fs.bci < an.getStart() + an.getLength()) {
                        Node localNode = fs.localAt(an.getIndex());

                        if (!parallelNodes.containsKey(localNode)) {
                            // Tornado.info("found parallel node:
                            // %s",localNode);
                            parallelNodes.put(localNode, an);
                        }
                    }
                }
            }
        });

        if (graph.hasLoops()) {

            final LoopsData data = new LoopsData(graph);
            data.detectedCountedLoops();

            int loopIndex = 0;
            final List<LoopEx> loops = data.outerFirst();
            if (TORNADO_LOOPS_REVERSE) {
                Collections.reverse(loops);
            }
            // final List<LoopEx> loops = (TORNADO_LOOPS_REVERSE) ?
            // data.innerFirst() : data.outerFirst() Collections.reverse(loops);
            for (LoopEx loop : loops) {

                for (InductionVariable iv : loop.getInductionVariables().getValues()) {
                    if (!parallelNodes.containsKey(iv.valueNode())) {
                        continue;
                    }

                    ValueNode maxIterations = null;
                    List<IntegerLessThanNode> conditions = iv.valueNode().usages().filter(IntegerLessThanNode.class).snapshot();
                    if (conditions.size() == 1) {
                        final IntegerLessThanNode lessThan = conditions.get(0);
                        maxIterations = lessThan.getY();
                    } else {
                        Tornado.debug("Unable to parallelise: multiple uses of iv");
                        continue;
                    }

                    if (iv.isConstantInit() && iv.isConstantStride()) {

                        final ConstantNode newInit = graph.addWithoutUnique(ConstantNode.forInt((int) iv.constantInit()));
                        final ConstantNode newStride = graph.addWithoutUnique(ConstantNode.forInt((int) iv.constantStride()));

                        final ParallelOffsetNode offset = graph.addWithoutUnique(new ParallelOffsetNode(loopIndex, newInit));

                        final ParallelStrideNode stride = graph.addWithoutUnique(new ParallelStrideNode(loopIndex, newStride));

                        final ParallelRangeNode range = graph.addWithoutUnique(new ParallelRangeNode(loopIndex, maxIterations, offset, stride));

                        final ValuePhiNode phi = (ValuePhiNode) iv.valueNode();
                        final ValueNode oldStride = phi.singleBackValueOrThis(); // was
                                                                                 // singleBackValue()

                        // System.out.printf("oldStride:
                        // %s\n",oldStride.toString());
                        if (oldStride.usages().count() > 1) {
                            final ValueNode duplicateStride = (ValueNode) oldStride.copyWithInputs(true);

                            oldStride.replaceAtMatchingUsages(duplicateStride, usage -> !usage.equals(phi));

                            // duplicateStride.removeUsage(phi);
                            // oldStride.removeUsage(node)
                        }

                        iv.initNode().replaceAtMatchingUsages(offset, node -> node.equals(phi));
                        iv.strideNode().replaceAtMatchingUsages(stride, node -> node.equals(oldStride));

                        // only replace this node in the loop condition
                        maxIterations.replaceAtMatchingUsages(range, node -> node.equals(conditions.get(0)));

                    } else {
                        Tornado.debug("Unable to parallelise: non-constant stride or offset");
                        continue;
                    }
                    loopIndex++;
                }

            }
        }
    }
}
