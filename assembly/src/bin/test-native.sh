#!/bin/bash

echo -e "\nTesting the Native OpenCL API\n"
tornado uk.ac.manchester.tornado.drivers.opencl.tests.TestOpenCLJITCompiler
tornado uk.ac.manchester.tornado.drivers.opencl.OpenCL


echo " " 