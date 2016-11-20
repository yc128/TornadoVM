package tornado.graal;

import com.oracle.graal.lir.phases.LIRPhaseSuite;
import com.oracle.graal.lir.phases.PostAllocationOptimizationPhase.PostAllocationOptimizationContext;
import com.oracle.graal.lir.phases.PreAllocationOptimizationPhase.PreAllocationOptimizationContext;
import com.oracle.graal.phases.common.AddressLoweringPhase.AddressLowering;
import com.oracle.graal.phases.common.CanonicalizerPhase.CustomCanonicalizer;
import tornado.graal.compiler.*;
import tornado.graal.phases.lir.TornadoAllocationStage;

public class TornadoSuites {

    private final TornadoSketchTier sketchTier;
    private final TornadoHighTier highTier;
    private final TornadoMidTier midTier;
    private final TornadoLowTier lowTier;

    private final TornadoAllocationStage allocStage;
    private final LIRPhaseSuite<PreAllocationOptimizationContext> preAllocStage;
    private final LIRPhaseSuite<PostAllocationOptimizationContext> postAllocStage;

    public TornadoSuites(TornadoCompilerConfiguration config, CustomCanonicalizer canonicalizer, AddressLowering addressLowering) {
        sketchTier = config.createSketchTier(canonicalizer);
        highTier = config.createHighTier(canonicalizer);
        midTier = config.createMidTier();
        lowTier = config.createLowTier(addressLowering);
        allocStage = config.createAllocationStage();
        preAllocStage = config.createPreAllocationOptimizationStage();
        postAllocStage = config.createPostAllocationOptimizationStage();
    }

    public TornadoSketchTier getSketchTier() {
        return sketchTier;
    }

    public TornadoHighTier getHighTier() {
        return highTier;
    }

    public TornadoMidTier getMidTier() {
        return midTier;
    }

    public TornadoLowTier getLowTier() {
        return lowTier;
    }

    public LIRPhaseSuite<PreAllocationOptimizationContext> getPreAllocationOptimizationStage() {
        return preAllocStage;
    }

    public TornadoAllocationStage getAllocationStage() {
        return allocStage;
    }

    public LIRPhaseSuite<PostAllocationOptimizationContext> getPostAllocationOptimizationStage() {
        return postAllocStage;
    }

    public TornadoLIRSuites getLIRSuites() {
        return new TornadoLIRSuites(preAllocStage, allocStage, postAllocStage);
    }

}
