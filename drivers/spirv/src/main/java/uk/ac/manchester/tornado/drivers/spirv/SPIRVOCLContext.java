package uk.ac.manchester.tornado.drivers.spirv;

import uk.ac.manchester.tornado.drivers.opencl.OCLExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

public class SPIRVOCLContext extends SPIRVContext {

    private OCLExecutionEnvironment context;
    private List<SPIRVOCLDeviceContext> spirvoclDeviceContext;

    public SPIRVOCLContext(SPIRVPlatform platform, List<SPIRVDevice> devices, OCLExecutionEnvironment context) {
        super(platform, devices);
        this.context = context;

        // Create a command queue per device;
        for (int deviceIndex = 0; deviceIndex < devices.size(); deviceIndex++) {
            context.createCommandQueue(deviceIndex);
        }

        spirvoclDeviceContext = new ArrayList<>();
        for (SPIRVDevice device : devices) {
            // We do not need command queue from this class, it was already created in the
            // constructor
            SPIRVOCLDeviceContext deviceContext = new SPIRVOCLDeviceContext(device, null, context);
            device.setDeviContext(deviceContext);
            spirvoclDeviceContext.add(deviceContext);
        }
    }

    @Override
    public SPIRVDeviceContext getDeviceContext(int deviceIndex) {
        return spirvoclDeviceContext.get(deviceIndex);
    }

    @Override
    public SPIRVCommandQueue createCommandQueue(int deviceIndex) {
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public SPIRVCommandQueue getCommandQueueForDevice(int deviceIndex) {
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public long allocateMemory(long numBytes) {
        throw new RuntimeException("Unimplemented");
    }

}
