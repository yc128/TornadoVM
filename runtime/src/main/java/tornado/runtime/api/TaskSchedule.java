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
package tornado.runtime.api;

import tornado.api.Event;
import tornado.common.SchedulableTask;
import tornado.common.TornadoDevice;
import tornado.common.enums.Access;
import tornado.runtime.api.TornadoFunctions.Task1;
import tornado.runtime.api.TornadoFunctions.Task10;
import tornado.runtime.api.TornadoFunctions.Task15;
import tornado.runtime.api.TornadoFunctions.Task2;
import tornado.runtime.api.TornadoFunctions.Task3;
import tornado.runtime.api.TornadoFunctions.Task4;
import tornado.runtime.api.TornadoFunctions.Task5;
import tornado.runtime.api.TornadoFunctions.Task6;
import tornado.runtime.api.TornadoFunctions.Task7;
import tornado.runtime.api.TornadoFunctions.Task8;
import tornado.runtime.api.TornadoFunctions.Task9;

public class TaskSchedule extends AbstractTaskGraph {

    private Event event;

    public TaskSchedule(String name) {
        super(name);
    }

    public <T1> TaskSchedule task(String id, Task1<T1> code, T1 arg) {
        addInner(TaskUtils.createTask(meta(), id, code, arg));
        return this;
    }

    public <T1, T2> TaskSchedule task(String id, Task2<T1, T2> code, T1 arg1, T2 arg2) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2));
        return this;
    }

    public <T1, T2, T3> TaskSchedule task(String id, Task3<T1, T2, T3> code, T1 arg1, T2 arg2,
            T3 arg3) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3));
        return this;
    }

    public <T1, T2, T3, T4> TaskSchedule task(String id, Task4<T1, T2, T3, T4> code, T1 arg1,
            T2 arg2, T3 arg3, T4 arg4) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4));
        return this;
    }

    public <T1, T2, T3, T4, T5> TaskSchedule task(String id, Task5<T1, T2, T3, T4, T5> code,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5));
        return this;
    }

    public <T1, T2, T3, T4, T5, T6> TaskSchedule task(String id,
            Task6<T1, T2, T3, T4, T5, T6> code, T1 arg1, T2 arg2, T3 arg3,
            T4 arg4, T5 arg5, T6 arg6) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5,
                arg6));
        return this;
    }

    public <T1, T2, T3, T4, T5, T6, T7> TaskSchedule task(String id,
            Task7<T1, T2, T3, T4, T5, T6, T7> code, T1 arg1, T2 arg2, T3 arg3,
            T4 arg4, T5 arg5, T6 arg6, T7 arg7) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5,
                arg6, arg7));
        return this;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8> TaskSchedule task(String id,
            Task8<T1, T2, T3, T4, T5, T6, T7, T8> code, T1 arg1, T2 arg2,
            T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5,
                arg6, arg7, arg8));
        return this;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9> TaskSchedule task(String id,
            Task9<T1, T2, T3, T4, T5, T6, T7, T8, T9> code, T1 arg1, T2 arg2,
            T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5,
                arg6, arg7, arg8, arg9));
        return this;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> TaskSchedule task(String id,
            Task10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> code, T1 arg1,
            T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8,
            T9 arg9, T10 arg10) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5,
                arg6, arg7, arg8, arg9, arg10));
        return this;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> TaskSchedule task(String id,
            Task15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> code, T1 arg1,
            T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8,
            T9 arg9, T10 arg10, T11 arg11, T12 arg12, T13 arg13, T14 arg14, T15 arg15) {
        addInner(TaskUtils.createTask(meta(), id, code, arg1, arg2, arg3, arg4, arg5,
                arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15));
        return this;
    }

    public TaskSchedule prebuiltTask(String id, String entryPoint, String filename, Object[] args, Access[] accesses, TornadoDevice device, int[] dimensions) {
        addInner(TaskUtils.createTask(meta(), id,
                entryPoint,
                filename,
                args,
                accesses,
                device,
                dimensions));
        return this;
    }

    @Deprecated
    public TaskSchedule add(SchedulableTask task) {
        addInner(task);
        return this;
    }

    public TaskSchedule task(SchedulableTask task) {
        addInner(task);
        return this;
    }

//    public <T> TaskSchedule reduce(String id, ReduceOp op, T[] values) {
//
//        return this;
//    }
    public TaskSchedule mapAllTo(TornadoDevice device) {
        setDevice(device);
        return this;
    }

    public TaskSchedule streamIn(Object... objects) {
        streamInInner(objects);
        return this;
    }

    public TaskSchedule streamOut(Object... objects) {
        streamOutInner(objects);
        return this;
    }

    public TaskSchedule schedule() {
        scheduleInner();
        return this;
    }

    public void execute() {
        schedule().waitOn();
    }

    public void printCompileTimes() {

    }

}
