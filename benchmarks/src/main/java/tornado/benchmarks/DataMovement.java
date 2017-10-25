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
package tornado.benchmarks;

import tornado.common.DeviceObjectState;
import tornado.common.TornadoDevice;
import tornado.runtime.TornadoDriver;
import tornado.runtime.TornadoRuntime;
import tornado.runtime.api.GlobalObjectState;

import static tornado.runtime.TornadoRuntime.getTornadoRuntime;

public class DataMovement {

    public static Object createArray(String type, int size) {

        switch (type) {
            case "i8":
                return new byte[size];
            case "i32":
                return new int[size];
            case "i64":
                return new long[size];
            case "f32":
                return new float[size];
            case "f64":
                return new double[size];

            default:
                System.err.printf("type %s is not supported", type);
                System.exit(-1);
        }
        return null;
    }

    private static TornadoDevice resolveDevice(TornadoRuntime runtime, String device) {
        final String[] ids = device.split(":");
        final TornadoDriver driver = runtime.getDriver(Integer.parseInt(ids[0]));
        return driver.getDevice(Integer.parseInt(ids[1]));
    }

    public static void main(String args[]) {
        final int startSize = Integer.parseInt(System.getProperty("startsize", "2"));
        final int endSize = Integer.parseInt(System.getProperty("endsize", "8192"));
        final int iterations = Integer.parseInt(System.getProperty("iterations", "100"));
        final String[] types = System.getProperty("types", "i8,i32,i64,f32,f64").split(",");

        final String[] devices = System.getProperty("devices", "0:0").split(",");

        System.out.println("device,type,numelements,numbytes,iterations,streamInElapsed,streamOutElapsed");

        for (final String deviceStr : devices) {
            TornadoRuntime runtime = getTornadoRuntime();
            final TornadoDevice device = resolveDevice(runtime, deviceStr);

            for (final String type : types) {
                for (int size = startSize; size <= endSize; size <<= 1) {

                    final Object array = createArray(type, size);
                    final GlobalObjectState globalState = runtime.resolveObject(array);
                    final DeviceObjectState deviceState = globalState.getDeviceState(device);

                    device.ensureAllocated(array, deviceState);

                    final long t0 = System.nanoTime();
                    for (int i = 0; i < iterations; i++) {
                        device.streamIn(array, deviceState);
                    }
                    device.sync();
                    final long t1 = System.nanoTime();
                    final double streamInElapsed = (t1 - t0) * 1e-9;

                    final long t2 = System.nanoTime();
                    for (int i = 0; i < iterations; i++) {
                        device.streamOut(array, deviceState, null);
                    }
                    device.sync();
                    final long t3 = System.nanoTime();
                    final double streamOutElapsed = (t3 - t2) * 1e-9;

                    final long numBytes = size * (Integer.parseInt(type.substring(1)) / 8);

                    System.out.printf("%s,%s,%d,%d,%d,%.9f,%.9f\n", device.getDeviceName(), type, size, numBytes, iterations, streamInElapsed, streamOutElapsed);
                    runtime.clearObjectState();
                    device.reset();

                    if (size == 0) {
                        size++;
                    }
                }
            }
        }
    }

}
