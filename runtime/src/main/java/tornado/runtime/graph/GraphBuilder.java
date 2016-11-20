package tornado.runtime.graph;

import java.nio.ByteBuffer;
import java.util.List;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import tornado.common.SchedulableTask;
import tornado.common.enums.Access;
import tornado.common.exceptions.TornadoInternalError;
import tornado.runtime.api.CompilableTask;
import tornado.runtime.api.LocalObjectState;
import tornado.runtime.api.TaskGraph;
import tornado.runtime.graph.nodes.*;
import tornado.runtime.sketcher.Sketch;
import tornado.runtime.sketcher.TornadoSketcher;

import static tornado.runtime.TornadoRuntime.getTornadoRuntime;

public class GraphBuilder {

    public static Graph buildGraph(ExecutionContext graphContext, ByteBuffer buffer) {
        Graph graph = new Graph();
//        DeviceMapping device;
        Access[] accesses = null;
        SchedulableTask task;
        AbstractNode[] args = null;
        ContextNode context = null;
        TaskNode taskNode = null;
        int argIndex = 0;
        int taskIndex = 0;

        final List<Object> constants = graphContext.getConstants();
        final List<Object> objects = graphContext.getObjects();

        final ConstantNode[] constantNodes = new ConstantNode[constants.size()];
        for (int i = 0; i < constants.size(); i++) {
            constantNodes[i] = new ConstantNode(i);
            graph.add(constantNodes[i]);
        }

        final AbstractNode[] objectNodes = new AbstractNode[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            objectNodes[i] = new ObjectNode(i);
            graph.add(objectNodes[i]);
        }

//		final ByteBuffer buffer = ByteBuffer.wrap(hlcode);
//		buffer.order(ByteOrder.LITTLE_ENDIAN);
//		buffer.limit(hlBuffer.position());
//		System.out.printf("task graph: bytes=%d\n", buffer.limit());
        final List<LocalObjectState> states = graphContext.getObjectStates();

        boolean shouldExit = false;
        while (!shouldExit && buffer.hasRemaining()) {
            final byte op = buffer.get();
//			System.out.printf("op: 0x%x\n", op);

            if (op == TaskGraph.ARG_LIST) {
                final int size = buffer.getInt();
//				System.out.printf("task graph: ARG_LIST %d\n", size);
                args = new AbstractNode[size];
                argIndex = 0;
                taskNode = new TaskNode(context, taskIndex, args);
            } else if (op == TaskGraph.LOAD_REF) {
                final int variableIndex = buffer.getInt();

                final AbstractNode arg = objectNodes[variableIndex];

                if (!(arg instanceof AsyncNode)) {

                    if (accesses[argIndex] == Access.WRITE) {
                        final AllocateNode allocateNode = new AllocateNode(context);
                        allocateNode.setValue((ObjectNode) arg);
                        graph.add(allocateNode);
                        context.addUse(allocateNode);
                        args[argIndex] = allocateNode;
                    } else {
                        final ObjectNode objectNode = (ObjectNode) arg;
                        final LocalObjectState state = states.get(objectNode.getIndex());
                        if (state.isStreamIn()) {
                            final StreamInNode streamInNode = new StreamInNode(context);
                            streamInNode.setValue(objectNode);
                            graph.add(streamInNode);
                            context.addUse(streamInNode);
                            args[argIndex] = streamInNode;
                        } else {
                            final CopyInNode copyInNode = new CopyInNode(context);
                            copyInNode.setValue((ObjectNode) arg);
                            graph.add(copyInNode);
                            context.addUse(copyInNode);
                            args[argIndex] = copyInNode;
                        }
                    }

                } else {
                    args[argIndex] = arg;
                }

                final AbstractNode nextAccessNode;
                if (accesses[argIndex] == Access.WRITE || accesses[argIndex] == Access.READ_WRITE) {
                    final DependentReadNode depRead = new DependentReadNode(context);

                    final ObjectNode value;
                    if (objectNodes[variableIndex] instanceof ObjectNode) {
                        value = (ObjectNode) objectNodes[variableIndex];
                    } else if (objectNodes[variableIndex] instanceof DependentReadNode) {
                        value = ((DependentReadNode) objectNodes[variableIndex]).getValue();
                    } else if (objectNodes[variableIndex] instanceof CopyInNode) {
                        value = ((CopyInNode) objectNodes[variableIndex]).getValue();
                    } else if (objectNodes[variableIndex] instanceof AllocateNode) {
                        value = ((AllocateNode) objectNodes[variableIndex]).getValue();
                    } else {
                        value = null;
                    }
                    depRead.setValue(value);
                    depRead.setDependent(taskNode);
                    graph.add(depRead);
                    nextAccessNode = depRead;
//					System.out.printf("updating object nodes[%d]: %s -> %s\n",variableIndex,objectNodes[variableIndex],depRead);
                } else {
                    nextAccessNode = args[argIndex];
                }

                objectNodes[variableIndex] = nextAccessNode;

//				System.out.printf("task graph: arg[%d] = %s\n", argIndex,
//						args[argIndex]);
                argIndex++;
            } else if (op == TaskGraph.LOAD_PRIM) {
                final int variableIndex = buffer.getInt();

                args[argIndex] = constantNodes[variableIndex];

//				System.out.printf("task graph: arg[%d] = %s\n", argIndex,
//						args[argIndex]);
                argIndex++;
            } else if (op == TaskGraph.LAUNCH) {
//				System.out.printf("task graph: launch task %s\n",
//						task.getName());

                context.addUse(taskNode);
                graph.add(taskNode);

            } else if (op == TaskGraph.CONTEXT) {
                final int globalTaskId = buffer.getInt();
//                device = graphContext.getDeviceForTask(globalTaskId);

                taskIndex = buffer.getInt();
                task = graphContext.getTask(taskIndex);

                context = graph.addUnique(new ContextNode(graphContext.getDeviceIndexForTask(globalTaskId)));

                if (task instanceof CompilableTask) {
                    final ResolvedJavaMethod resolvedMethod = getTornadoRuntime()
                            .resolveMethod(((CompilableTask) task).getMethod());
                    Sketch sketch = TornadoSketcher.lookup(resolvedMethod);
                    accesses = sketch.getMeta().getArgumentsAccess();
                } else {

                    accesses = task.getArgumentsAccess();
                }

//				System.out.printf("task graph: new frame on %s for %s\n",
//						device, task.getName());
            } else {
//				System.out.printf("task graph: invalid op 0x%x\n", op);
                shouldExit = true;
            }
        }

        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).isStreamOut()) {
                if (objectNodes[i] instanceof DependentReadNode) {
//				System.out.printf("stream out: on=%s, state=%s, object=%s\n",objectNodes[i],states.get(i),graphContext.getObjects().get(i));
                    final DependentReadNode readNode = (DependentReadNode) objectNodes[i];
                    context = readNode.getContext();
                    final CopyOutNode copyOutNode = new CopyOutNode(context);
                    copyOutNode.setValue(readNode);
                    graph.add(copyOutNode);
                    context.addUse(copyOutNode);
                } else {
                    // object is not modified
                }
            } else if (states.get(i).isStreamIn() && objectNodes[i] instanceof ObjectNode) {
                TornadoInternalError.guarantee(graphContext.getDevices().size() == 1, "unsupported StreamIn operation in multiple device mode");
                final StreamInNode streamInNode = new StreamInNode(context);
                streamInNode.setValue((ObjectNode) objectNodes[i]);
                graph.add(streamInNode);
                context.addUse(streamInNode);
            }
        }

//		System.out.printf("task graph: error=%s\n", shouldExit);
        return graph;
    }
}
