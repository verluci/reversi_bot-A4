/*
 * JOCL - Java bindings for OpenCL
 * https://github.com/gpu/JOCLSamples/blob/master/src/main/java/org/jocl/samples/JOCLDeviceQuery.java
 * https://github.com/gpu/JOCLSamples/blob/master/src/main/java/org/jocl/samples/JOCLSample.java
 * Copyright 2009-2019 Marco Hutter - http://www.jocl.org/
 */

package com.github.lutzenh.gpgpu;

import static org.jocl.CL.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Formatter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jocl.*;

/**
 * A small JOCL sample.
 */
public class JOCLSample
{
    /**
     * The source code of the OpenCL program to execute
     */

    //region OpenCL Device Information

    public static ObservableList<GraphicsDevice> getGraphicsDevices() {
        ObservableList<GraphicsDevice> graphicsDevices = FXCollections.observableArrayList();

        cl_platform_id platforms[] = new cl_platform_id[0];
        try {
            // Obtain the number of platforms
            int numPlatforms[] = new int[1];
            clGetPlatformIDs(0, null, numPlatforms);

            // Obtain the platform IDs
            platforms = new cl_platform_id[numPlatforms[0]];
            clGetPlatformIDs(platforms.length, platforms, null);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("INFO: Could not initialize native OpenCL library:\n"+
                    "\t- OpenCL will be disabled.");
        }

        // Collect all devices of all platforms
        for (int i=0; i<platforms.length; i++)
        {
            String platformName = getString(platforms[i], CL_PLATFORM_NAME);

            // Obtain the number of devices for the current platform
            int numDevices[] = new int[1];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, 0, null, numDevices);

            // CL_PLATFORM_VERSION
            String platformVersion = getString(platforms[i], CL_PLATFORM_VERSION);

            cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, numDevices[0], devicesArray, null);

            for (int y = 0; y < devicesArray.length; y++) {
                String deviceName = getString(devicesArray[y], CL_DEVICE_NAME);
                String openclVersion = getString(devicesArray[y], CL_DEVICE_OPENCL_C_VERSION);
                String deviceVendor = getString(devicesArray[y], CL_DEVICE_VENDOR);
                String driverVersion = getString(devicesArray[y], CL_DRIVER_VERSION);
                DeviceType deviceType = DeviceType.INVALID;

                long chosenDeviceType = getLong(devicesArray[y], CL_DEVICE_TYPE);
                if( (chosenDeviceType & CL_DEVICE_TYPE_CPU) != 0)
                    deviceType = DeviceType.CPU;
                if( (chosenDeviceType & CL_DEVICE_TYPE_GPU) != 0)
                    deviceType = DeviceType.GPU;
                if( (chosenDeviceType & CL_DEVICE_TYPE_ACCELERATOR) != 0)
                    deviceType = DeviceType.ACCELERATOR;
                if( (chosenDeviceType & CL_DEVICE_TYPE_DEFAULT) != 0)
                    deviceType = DeviceType.DEFAULT;

                GraphicsDevice device = new GraphicsDevice(
                        devicesArray[y],
                        platforms[i],
                        deviceName,
                        openclVersion,
                        deviceVendor,
                        driverVersion,
                        deviceType,
                        platformName,
                        platformVersion);

                graphicsDevices.add(device);
            }
        }

        return graphicsDevices;
    }

    public static String getDeviceSpecifications(cl_device_id device) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter fmt = new Formatter(stringBuilder);

        // CL_DEVICE_NAME
        String deviceName = getString(device, CL_DEVICE_NAME);
        fmt.format("CL_DEVICE_NAME=%s\n", deviceName);

        // CL_DEVICE_VENDOR
        String deviceVendor = getString(device, CL_DEVICE_VENDOR);
        fmt.format("CL_DEVICE_VENDOR=%s\n", deviceVendor);

        // CL_DRIVER_VERSION
        String driverVersion = getString(device, CL_DRIVER_VERSION);
        fmt.format("CL_DRIVER_VERSION=%s\n", driverVersion);

        // CL_DEVICE_TYPE
        long deviceType = getLong(device, CL_DEVICE_TYPE);
        if( (deviceType & CL_DEVICE_TYPE_CPU) != 0)
            fmt.format("CL_DEVICE_TYPE=%s\n", "CL_DEVICE_TYPE_CPU");
        if( (deviceType & CL_DEVICE_TYPE_GPU) != 0)
            fmt.format("CL_DEVICE_TYPE=%s\n", "CL_DEVICE_TYPE_GPU");
        if( (deviceType & CL_DEVICE_TYPE_ACCELERATOR) != 0)
            fmt.format("CL_DEVICE_TYPE=%s\n", "CL_DEVICE_TYPE_ACCELERATOR");
        if( (deviceType & CL_DEVICE_TYPE_DEFAULT) != 0)
            fmt.format("CL_DEVICE_TYPE=%s\n", "CL_DEVICE_TYPE_DEFAULT");

        // CL_DEVICE_MAX_COMPUTE_UNITS
        int maxComputeUnits = getInt(device, CL_DEVICE_MAX_COMPUTE_UNITS);
        fmt.format("CL_DEVICE_MAX_COMPUTE_UNITS=%d\n", maxComputeUnits);

        // CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS
        long maxWorkItemDimensions = getLong(device, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
        fmt.format("CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS=%d\n", maxWorkItemDimensions);

        // CL_DEVICE_MAX_WORK_ITEM_SIZES
        long[] maxWorkItemSizes = getSizes(device, CL_DEVICE_MAX_WORK_ITEM_SIZES, 3);
        fmt.format("CL_DEVICE_MAX_WORK_ITEM_SIZES=%d / %d / %d \n",
                maxWorkItemSizes[0], maxWorkItemSizes[1], maxWorkItemSizes[2]);

        // CL_DEVICE_MAX_WORK_GROUP_SIZE
        long maxWorkGroupSize = getSize(device, CL_DEVICE_MAX_WORK_GROUP_SIZE);
        fmt.format("CL_DEVICE_MAX_WORK_GROUP_SIZE=%d\n", maxWorkGroupSize);

        // CL_DEVICE_MAX_CLOCK_FREQUENCY
        long maxClockFrequency = getLong(device, CL_DEVICE_MAX_CLOCK_FREQUENCY);
        fmt.format("CL_DEVICE_MAX_CLOCK_FREQUENCY=%d MHz\n", maxClockFrequency);

        // CL_DEVICE_ADDRESS_BITS
        int addressBits = getInt(device, CL_DEVICE_ADDRESS_BITS);
        fmt.format("CL_DEVICE_ADDRESS_BITS=%d\n", addressBits);

        // CL_DEVICE_MAX_MEM_ALLOC_SIZE
        long maxMemAllocSize = getLong(device, CL_DEVICE_MAX_MEM_ALLOC_SIZE);
        fmt.format("CL_DEVICE_MAX_MEM_ALLOC_SIZE=%d MByte\n", (int)(maxMemAllocSize / (1024 * 1024)));

        // CL_DEVICE_GLOBAL_MEM_SIZE
        long globalMemSize = getLong(device, CL_DEVICE_GLOBAL_MEM_SIZE);
        fmt.format("CL_DEVICE_GLOBAL_MEM_SIZE=%d MByte\n", (int)(globalMemSize / (1024 * 1024)));

        // CL_DEVICE_ERROR_CORRECTION_SUPPORT
        int errorCorrectionSupport = getInt(device, CL_DEVICE_ERROR_CORRECTION_SUPPORT);
        fmt.format("CL_DEVICE_ERROR_CORRECTION_SUPPORT=%s\n", errorCorrectionSupport != 0 ? "yes" : "no");

        // CL_DEVICE_LOCAL_MEM_TYPE
        int localMemType = getInt(device, CL_DEVICE_LOCAL_MEM_TYPE);
        fmt.format("CL_DEVICE_LOCAL_MEM_TYPE=%s\n", localMemType == 1 ? "local" : "global");

        // CL_DEVICE_LOCAL_MEM_SIZE
        long localMemSize = getLong(device, CL_DEVICE_LOCAL_MEM_SIZE);
        fmt.format("CL_DEVICE_LOCAL_MEM_SIZE=%d KByte\n", (int)(localMemSize / 1024));

        // CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE
        long maxConstantBufferSize = getLong(device, CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE);
        fmt.format("CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE=%d KByte\n", (int)(maxConstantBufferSize / 1024));

        // CL_DEVICE_QUEUE_PROPERTIES
        long queueProperties = getLong(device, CL_DEVICE_QUEUE_ON_HOST_PROPERTIES);
        if(( queueProperties & CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE ) != 0)
            fmt.format("CL_DEVICE_QUEUE_PROPERTIES=%s\n", "CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE");
        if(( queueProperties & CL_QUEUE_PROFILING_ENABLE ) != 0)
            fmt.format("CL_DEVICE_QUEUE_PROPERTIES=%s\n", "CL_QUEUE_PROFILING_ENABLE");

        // CL_DEVICE_IMAGE_SUPPORT
        int imageSupport = getInt(device, CL_DEVICE_IMAGE_SUPPORT);
        fmt.format("CL_DEVICE_IMAGE_SUPPORT=%d\n", imageSupport);

        // CL_DEVICE_MAX_READ_IMAGE_ARGS
        int maxReadImageArgs = getInt(device, CL_DEVICE_MAX_READ_IMAGE_ARGS);
        fmt.format("CL_DEVICE_MAX_READ_IMAGE_ARGS=%d\n", maxReadImageArgs);

        // CL_DEVICE_MAX_WRITE_IMAGE_ARGS
        int maxWriteImageArgs = getInt(device, CL_DEVICE_MAX_WRITE_IMAGE_ARGS);
        fmt.format("CL_DEVICE_MAX_WRITE_IMAGE_ARGS=%d\n", maxWriteImageArgs);

        // CL_DEVICE_SINGLE_FP_CONFIG
        long singleFpConfig = getLong(device, CL_DEVICE_SINGLE_FP_CONFIG);
        fmt.format("CL_DEVICE_SINGLE_FP_CONFIG=%s\n",
                stringFor_cl_device_fp_config(singleFpConfig));

        // CL_DEVICE_IMAGE2D_MAX_WIDTH
        long image2dMaxWidth = getSize(device, CL_DEVICE_IMAGE2D_MAX_WIDTH);
        fmt.format("CL_DEVICE_2D_MAX_WIDTH=%d\n", image2dMaxWidth);

        // CL_DEVICE_IMAGE2D_MAX_HEIGHT
        long image2dMaxHeight = getSize(device, CL_DEVICE_IMAGE2D_MAX_HEIGHT);
        fmt.format("CL_DEVICE_2D_MAX_HEIGHT=%d\n", image2dMaxHeight);

        // CL_DEVICE_IMAGE3D_MAX_WIDTH
        long image3dMaxWidth = getSize(device, CL_DEVICE_IMAGE3D_MAX_WIDTH);
        fmt.format("CL_DEVICE_3D_MAX_WIDTH=%d\n", image3dMaxWidth);

        // CL_DEVICE_IMAGE3D_MAX_HEIGHT
        long image3dMaxHeight = getSize(device, CL_DEVICE_IMAGE3D_MAX_HEIGHT);
        fmt.format("CL_DEVICE_3D_MAX_HEIGHT=%d\n", image3dMaxHeight);

        // CL_DEVICE_IMAGE3D_MAX_DEPTH
        long image3dMaxDepth = getSize(device, CL_DEVICE_IMAGE3D_MAX_DEPTH);
        fmt.format("CL_DEVICE_3D_MAX_DEPTH=%d\n", image3dMaxDepth);

        // CL_DEVICE_PREFERRED_VECTOR_WIDTH_<type>
        fmt.format("CL_DEVICE_PREFERRED_VECTOR_WIDTH_<t>=");
        int preferredVectorWidthChar = getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR);
        int preferredVectorWidthShort = getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT);
        int preferredVectorWidthInt = getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT);
        int preferredVectorWidthLong = getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG);
        int preferredVectorWidthFloat = getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT);
        int preferredVectorWidthDouble = getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE);
        fmt.format("CHAR %d, SHORT %d, INT %d, LONG %d, FLOAT %d, DOUBLE %d\n",
                preferredVectorWidthChar, preferredVectorWidthShort,
                preferredVectorWidthInt, preferredVectorWidthLong,
                preferredVectorWidthFloat, preferredVectorWidthDouble);

        return stringBuilder.toString();
    }

    public static boolean performSimpleGPUCalculationTest(cl_device_id device, cl_platform_id platform) {
        // Create input- and output data
        int n = 10;
        float srcArrayA[] = new float[n];
        float srcArrayB[] = new float[n];
        float dstArray[] = new float[n];
        for (int i=0; i<n; i++)
        {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        }
        Pointer srcA = Pointer.to(srcArrayA);
        Pointer srcB = Pointer.to(srcArrayB);
        Pointer dst = Pointer.to(dstArray);

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Create a context for the selected device
        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        cl_queue_properties properties = new cl_queue_properties();

        // For OpenCL 2.0 and higher
        //cl_command_queue commandQueue = clCreateCommandQueueWithProperties(
        //        context, device, properties, null);

        cl_command_queue commandQueue = clCreateCommandQueue(context, device, properties.getNativePointer(), null);

        // Allocate the memory objects for the input- and output data
        cl_mem srcMemA = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, srcA, null);
        cl_mem srcMemB = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, srcB, null);
        cl_mem dstMem = clCreateBuffer(context,
                CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);

        String content = "";

        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File file = new File(classLoader.getResource("simple_add_kernel.cl").getFile());
            content = new String(Files.readAllBytes(file.toPath()));
        }
        catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return false;
        }

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
                1, new String[]{ content }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);

        // Set the arguments for the kernel
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemA));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(srcMemB));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(dstMem));

        // Set the work-item dimensions
        long global_work_size[] = new long[]{n};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, dstMem, CL_TRUE, 0,
                n * Sizeof.cl_float, dst, 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(srcMemA);
        clReleaseMemObject(srcMemB);
        clReleaseMemObject(dstMem);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // Verify the result
        boolean passed = true;
        final float epsilon = 1e-7f;
        for (int i=0; i<n; i++)
        {
            float x = dstArray[i];
            float y = srcArrayA[i] * srcArrayB[i];
            boolean epsilonEqual = Math.abs(x - y) <= epsilon * Math.abs(x);
            if (!epsilonEqual)
            {
                passed = false;
                break;
            }
        }
        System.out.println("Test "+(passed?"PASSED":"FAILED"));
        if (n <= 10)
        {
            System.out.println("Result: "+ Arrays.toString(dstArray));
        }

        return passed;
    }

    //endregion

    //region OpenCL Information Retrieval

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static int getInt(cl_device_id device, int paramName)
    {
        return getInts(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static int[] getInts(cl_device_id device, int paramName, int numValues)
    {
        int values[] = new int[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_int * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_device_id device, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }

    /**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform The platform
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_platform_id platform, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static long getLong(cl_device_id device, int paramName)
    {
        return getLongs(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static long[] getLongs(cl_device_id device, int paramName, int numValues)
    {
        long values[] = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static long getSize(cl_device_id device, int paramName)
    {
        return getSizes(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    static long[] getSizes(cl_device_id device, int paramName, int numValues)
    {
        // The size of the returned data has to depend on
        // the size of a size_t, which is handled here
        ByteBuffer buffer = ByteBuffer.allocate(
                numValues * Sizeof.size_t).order(ByteOrder.nativeOrder());
        clGetDeviceInfo(device, paramName, Sizeof.size_t * numValues,
                Pointer.to(buffer), null);
        long values[] = new long[numValues];
        if (Sizeof.size_t == 4)
        {
            for (int i=0; i<numValues; i++)
            {
                values[i] = buffer.getInt(i * Sizeof.size_t);
            }
        }
        else
        {
            for (int i=0; i<numValues; i++)
            {
                values[i] = buffer.getLong(i * Sizeof.size_t);
            }
        }
        return values;
    }

    //endregion
}
