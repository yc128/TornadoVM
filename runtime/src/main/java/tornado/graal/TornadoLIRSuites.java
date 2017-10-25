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
package tornado.graal;

import tornado.graal.phases.lir.TornadoAllocationStage;

import org.graalvm.compiler.lir.phases.LIRPhaseSuite;
import org.graalvm.compiler.lir.phases.PostAllocationOptimizationPhase.PostAllocationOptimizationContext;
import org.graalvm.compiler.lir.phases.PreAllocationOptimizationPhase.PreAllocationOptimizationContext;

public class TornadoLIRSuites {

	private final LIRPhaseSuite<PreAllocationOptimizationContext> preAllocStage;
	private final TornadoAllocationStage allocStage;
	private final LIRPhaseSuite<PostAllocationOptimizationContext> postAllocStage;
	
	public TornadoLIRSuites(
			LIRPhaseSuite<PreAllocationOptimizationContext> preAllocStage,
			TornadoAllocationStage allocStage,
			LIRPhaseSuite<PostAllocationOptimizationContext> postAllocStage) {
		this.preAllocStage = preAllocStage;
		this.allocStage = allocStage;
		this.postAllocStage = postAllocStage;
	}

	public LIRPhaseSuite<PreAllocationOptimizationContext> getPreAllocationStage() {
		return preAllocStage;
	}

	public TornadoAllocationStage getAllocationStage() {
		return allocStage;
	}

	public LIRPhaseSuite<PostAllocationOptimizationContext> getPostAllocationStage() {
		return postAllocStage;
	}

}
