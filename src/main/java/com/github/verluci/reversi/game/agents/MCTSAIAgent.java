package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;
import com.github.verluci.reversi.gpgpu.JOCLSample;
import org.jocl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.jocl.CL.*;
import static org.jocl.CL.clReleaseContext;

public class MCTSAIAgent extends AIAgent {
    /**
     * A simple helper class for tracking cl_events and printing
     * timing information for the execution of the commands that
     * are associated with the events.
     */
    static class ExecutionStatistics
    {
        /**
         * A single entry of the ExecutionStatistics
         */
        private static class Entry
        {
            private String name;
            private long submitTime[] = new long[1];
            private long queuedTime[] = new long[1];
            private long startTime[] = new long[1];
            private long endTime[] = new long[1];

            Entry(String name, cl_event event)
            {
                this.name = name;
                CL.clGetEventProfilingInfo(
                        event, CL.CL_PROFILING_COMMAND_QUEUED,
                        Sizeof.cl_ulong, Pointer.to(queuedTime), null);
                CL.clGetEventProfilingInfo(
                        event, CL.CL_PROFILING_COMMAND_SUBMIT,
                        Sizeof.cl_ulong, Pointer.to(submitTime), null);
                CL.clGetEventProfilingInfo(
                        event, CL.CL_PROFILING_COMMAND_START,
                        Sizeof.cl_ulong, Pointer.to(startTime), null);
                CL.clGetEventProfilingInfo(
                        event, CL.CL_PROFILING_COMMAND_END,
                        Sizeof.cl_ulong, Pointer.to(endTime), null);
            }

            void normalize(long baseTime)
            {
                submitTime[0] -= baseTime;
                queuedTime[0] -= baseTime;
                startTime[0] -= baseTime;
                endTime[0] -= baseTime;
            }

            long getQueuedTime()
            {
                return queuedTime[0];
            }

            void print()
            {
                System.out.println("Event "+name+": ");
                System.out.println("Queued : "+
                        String.format("%8.3f", queuedTime[0]/1e6)+" ms");
                System.out.println("Submit : "+
                        String.format("%8.3f", submitTime[0]/1e6)+" ms");
                System.out.println("Start  : "+
                        String.format("%8.3f", startTime[0]/1e6)+" ms");
                System.out.println("End    : "+
                        String.format("%8.3f", endTime[0]/1e6)+" ms");

                long duration = endTime[0]-startTime[0];
                System.out.println("Time   : "+
                        String.format("%8.3f", duration / 1e6)+" ms");
            }
        }

        /**
         * The list of entries in this instance
         */
        private List<Entry> entries = new ArrayList<Entry>();

        /**
         * Adds the specified entry to this instance
         *
         * @param name A name for the event
         * @param event The event
         */
        public void addEntry(String name, cl_event event)
        {
            entries.add(new Entry(name, event));
        }

        /**
         * Removes all entries
         */
        public void clear()
        {
            entries.clear();
        }

        /**
         * Normalize the entries, so that the times are relative
         * to the time when the first event was queued
         */
        private void normalize()
        {
            long minQueuedTime = Long.MAX_VALUE;
            for (Entry entry : entries)
            {
                minQueuedTime = Math.min(minQueuedTime, entry.getQueuedTime());
            }
            for (Entry entry : entries)
            {
                entry.normalize(minQueuedTime);
            }
        }

        /**
         * Print the statistics
         */
        public void print()
        {
            normalize();
            for (Entry entry : entries)
            {
                entry.print();
            }
        }


    }

    @Override
    protected Tile findOptimalMove(GameBoard board) {
        return null;
    }

    private static int getOptimalMoveUsingOpenCL(cl_device_id device, cl_platform_id platform) {
        int NUMBER_OF_TRIES = 100*1024;
        int NUMBER_OF_TILES = 64;
        int NUMBER_OF_THREADS = NUMBER_OF_TRIES * 4; // Thread Factor, Amount, Number of possible moves.
        int NUMBER_OF_RANDOM_NUMBERS = NUMBER_OF_THREADS * 128;

        // The first player in this array should always be the starting player
        long[] players = {
            Long.parseLong("0000000000000000000000000001000000001000000000000000000000000000", 2),
            Long.parseLong("0000000000000000000000000000100000010000000000000000000000000000", 2)
        };

        long[] playerResult = new long[2];

        int[] possibleMoves = new int[NUMBER_OF_TILES];
        int[] randomNumberArray = new int[NUMBER_OF_RANDOM_NUMBERS];
        int[] resultArray = new int[NUMBER_OF_THREADS];

        // Fill the random-number-array with random values.
        Random random = new Random();
        for (int i=0; i < NUMBER_OF_RANDOM_NUMBERS; i++)
            randomNumberArray[i] = random.nextInt();

        // Locate pointers of the input values
        Pointer ptrPlayerTiles = Pointer.to(players);
        Pointer ptrPossibleMoves = Pointer.to(possibleMoves);
        Pointer ptrRandomNumbers = Pointer.to(randomNumberArray);
        // Locate the pointer of the output values.
        Pointer ptrResults = Pointer.to(resultArray);
        Pointer ptrPlayerTilesResult = Pointer.to(playerResult);

        //region Context

        // Enable exceptions and subsequently omit error checks.
        CL.setExceptionsEnabled(true);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Create a context for the selected device
        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        long properties = 0;
        properties |= CL.CL_QUEUE_PROFILING_ENABLE;

        // For OpenCL 2.0 and higher
        //cl_command_queue commandQueue = clCreateCommandQueueWithProperties(
        //        context, device, properties, null);

        cl_command_queue commandQueue = clCreateCommandQueue(context, device, properties, null);

        //endregion

        //region Memory allocation

        // Allocate the memory objects for the input- and output data
        cl_mem playerTilesMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_ulong * 2, ptrPlayerTiles, null);
        cl_mem possibleMovesMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * NUMBER_OF_TILES, ptrPossibleMoves, null);
        cl_mem randomNumbersMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * NUMBER_OF_RANDOM_NUMBERS, ptrRandomNumbers, null);
        cl_mem resultsMem = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_int * NUMBER_OF_THREADS, null, null);
        cl_mem playerTilesResultMem = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_ulong * 2, null, null);

        //endregion

        //region load kernel

        String content = "";
        try {
            File file = new File(ClassLoader.getSystemResource("mcts_reversi_kernel.cl").getFile());
            content = new String(Files.readAllBytes(file.toPath()));
        }
        catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
                1, new String[]{ content }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "mctsKernel", null);

        //endregion

        // Set the arguments for the kernel
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(playerTilesMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(possibleMovesMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(randomNumbersMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(resultsMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(playerTilesResultMem));

        // Set the work-item dimensions
        long[] global_work_size = new long[]{ NUMBER_OF_THREADS };

        cl_event work_event = new cl_event();
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, null, 0, null, work_event);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, resultsMem, CL_TRUE, 0,
                Sizeof.cl_int * NUMBER_OF_THREADS, ptrResults, 0, null, null);

        // Read the output of the first match.
        clEnqueueReadBuffer(commandQueue, playerTilesResultMem, CL_TRUE, 0,
                Sizeof.cl_ulong * 2, ptrPlayerTilesResult, 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(playerTilesMem);
        clReleaseMemObject(possibleMovesMem);
        clReleaseMemObject(randomNumbersMem);
        clReleaseMemObject(resultsMem);
        clReleaseMemObject(playerTilesResultMem);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        System.out.println(resultArray[0]);

        System.out.println(Long.toBinaryString(playerResult[0]));
        System.out.println(Long.toBinaryString(playerResult[1]));

        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.addEntry("mctsKernel", work_event);
        executionStatistics.print();

        return -1;
    }

    public static void main(String[] args) {
        var devices = JOCLSample.getGraphicsDevices();
        var chosenDevice = devices.get(0);

        System.out.println(chosenDevice.getName());

        int move = getOptimalMoveUsingOpenCL(chosenDevice.getId(), chosenDevice.getPlatform_id());
    }
}
