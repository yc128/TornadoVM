/*
 * This file is part of Tornado: A heterogeneous programming framework: 
 * https://github.com/beehive-lab/tornado
 *
 * Copyright (c) 2013-2017 APT Group, School of Computer Science, 
 * The University of Manchester
 *
 * This work is partially supported by EPSRC grants:
 * Anyscale EP/L000725/1 and PAMELA EP/K008730/1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: James Clarkson
 *
 */
package tornado.graal.nodes.logic;

import org.graalvm.compiler.graph.IterableNodeType;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.graph.spi.Canonicalizable;
import org.graalvm.compiler.graph.spi.CanonicalizerTool;
import org.graalvm.compiler.lir.gen.LIRGeneratorTool;
import org.graalvm.compiler.nodeinfo.InputType;
import org.graalvm.compiler.nodeinfo.NodeInfo;
import org.graalvm.compiler.nodes.LogicNode;
import org.graalvm.compiler.nodes.spi.NodeLIRBuilderTool;
import jdk.vm.ci.meta.Value;

@NodeInfo
public abstract class BinaryLogicalNode extends LogicNode implements IterableNodeType, Canonicalizable.Binary<LogicNode>, LogicalCompareNode {

    public static final NodeClass<BinaryLogicalNode> TYPE = NodeClass.create(BinaryLogicalNode.class);

    @Input(InputType.Condition)
    LogicNode x;
    @Input(InputType.Condition)
    LogicNode y;

    protected BinaryLogicalNode(NodeClass<? extends BinaryLogicalNode> type, LogicNode x, LogicNode y) {
        super(type);
        this.x = x;
        this.y = y;
    }

    @Override
    public final void generate(NodeLIRBuilderTool builder) {
        Value x = builder.operand(getX());
        Value y = builder.operand(getY());
        Value result = generate(builder.getLIRGeneratorTool(), x, y);
        builder.setResult(this, result);
    }

    abstract public Value generate(LIRGeneratorTool gen, Value x, Value y);

    @Override
    public LogicNode canonical(CanonicalizerTool tool) {
        return this;
    }

    @Override
    public Node canonical(CanonicalizerTool tool, LogicNode forX, LogicNode forY) {
        return this;
    }

    @Override
    public LogicNode getX() {
        return x;
    }

    @Override
    public LogicNode getY() {
        return y;
    }

}
