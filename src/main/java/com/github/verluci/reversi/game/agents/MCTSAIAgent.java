package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.gpgpu.GraphicsDevice;
import org.jocl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.jocl.CL.*;
import static org.jocl.CL.clReleaseContext;

/**
 * This class contains a GPU accelerated Othello AI based on the MCTS algorithm.
 * https://en.wikipedia.org/wiki/Monte_Carlo_tree_search
 *
 * For the explanation of the data allocation process see the method getOptimalMoveUsingOpenCL() in this class.
 * For the explanation of the executed kernel-code on the graphics-device see resources/mcts_reversi_kernel.cl
 */
public class MCTSAIAgent extends AIAgent {
    private final GraphicsDevice graphicsDevice;

    /**
     * Constructor for MCTSAIAgent
     * @param graphicsDevice The graphics device the games should be simulated on.
     */
    public MCTSAIAgent(GraphicsDevice graphicsDevice) {
        this.graphicsDevice = graphicsDevice;
    }

    /**
     * @param board The board on which the optimal tile should be found on.
     * @return The most optimal tile the MCTS-ai could find.
     */
    @Override
    protected Tile findOptimalMove(GameBoard board) {
        // Retrieve all possible moves from the current board.
        var moves = board.getTilesWithState(TileState.POSSIBLE_MOVE);

        // Put the moves in the following array: [ move_count, move1, move2, move3, move4, 0, 0, 0, ... ]
        int[] possibleMoves = new int[board.getXSize() * board.getYSize() + 1];
        possibleMoves[0] = moves.size();
        for (int i = 0; i < moves.size(); i++) {
            var move = moves.get(i);
            possibleMoves[i+1] = (move.getYCoordinate() * board.getXSize()) + move.getXCoordinate();
        }

        // Retrieve the player tiles as a 64-bit (u)long.
        long player1 = board.getPlayerTilesLongValue(Game.getTileStateUsingPlayer(player));
        long player2 = board.getPlayerTilesLongValue(Game.getInvertedTileStateUsingPlayer(player));

        // Choose the amount of simulations based on the current state of the game and the strength of the GraphicsDevice.
        int threadCount = calculateThreadCount(graphicsDevice, board);

        // Estimate the most optimal move with OpenCL using the provided GraphicsDevice and threadCount.
        int move = getOptimalMoveUsingOpenCL(graphicsDevice, player1, player2, possibleMoves, threadCount);

        // Convert the retrieved optimal tile-index to an x and y coordinate
        int x = move % board.getXSize();
        int y = move / board.getXSize();

        // Return the estimated optimal move.
        return board.getTile(x, y);
    }

    @Override
    public void setGame(Game game) {
        if(game instanceof OthelloGame)
            super.setGame(game);
        else
            throw new IllegalArgumentException("This MCTS-AI can only be used for Othello/Reversi!");
    }

    /**
     * This methods retrieves the most optimal tile it can find by using NUMBER_OF_THREADS amount of simulations.
     * The method first creates three arrays containing: the current state of the board, random numbers and an array
     * where the calculated results can be copied back into.
     *
     * After this, the arrays will be allocated and copied as buffers to the given GraphicsDevice.
     * The GraphicsDevice will then perform the NUMBER_OF_THREADS simulations.
     * see 'resources/mcts_reversi_kernel.cl for' the explanation of the kernel executions of the simulations.
     *
     * When the simulations are done, the results are copied into the resultArray on the host-machine and the
     * allocated buffers will be released on the GraphicsDevice. The host machine will then sum all wins/draws/loses of a
     * selected path and will choose the path with the least amount of loses.
     *
     * @param graphicsDevice The graphics-device on which the simulations should be performed.
     * @param player The player1 in the OthelloGame also known as black.
     * @param opponent The player2 in the OthelloGame also known as white.
     * @param possibleMoves An array of size 65 in which the first value is the count of possible moves, following all
     *                      possible moves. example: [ 4, 15, 13, 12, 8, 0, 0, 0, ... ]
     * @param threadCount The amount of threads x 1024 that the GraphicsDevice is going to simulate.
     * @return The most optimal move the AI was able to find.
     */
    private static int getOptimalMoveUsingOpenCL(GraphicsDevice graphicsDevice, long player, long opponent, int[] possibleMoves, int threadCount) {
        var platform = graphicsDevice.getPlatform_id();
        var device = graphicsDevice.getId();

        // The amount of threads that should be executed on the graphics-device.
        final int NUMBER_OF_THREADS = threadCount * 1024;
        // The graphics-device should contains 64 random numbers per simulation (for every possible move 1 random number).
        final int RANDOM_NUMBER_COUNT = NUMBER_OF_THREADS * 64;

        // The first player in this array should always be the starting player
        long[] players = {
                player,
                opponent
        };

        int[] randomNumberArray = new int[RANDOM_NUMBER_COUNT];
        int[] resultArray = new int[NUMBER_OF_THREADS];

        // Fill the random-number-array with random values.
        Random random = new Random();
        for (int i=0; i < RANDOM_NUMBER_COUNT; i++)
            randomNumberArray[i] = random.nextInt();

        // Locate pointers of the input values
        Pointer ptrPlayerTiles = Pointer.to(players);
        Pointer ptrPossibleMoves = Pointer.to(possibleMoves);
        Pointer ptrRandomNumbers = Pointer.to(randomNumberArray);
        // Locate the pointer of the output values.
        Pointer ptrResults = Pointer.to(resultArray);

        //region Context

        // Enable exceptions and subsequently omit error checks.
        CL.setExceptionsEnabled(true);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Create a context for the selected device
        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{ device },
                null, null, null);

        // Create a command-queue for the selected device
        long properties = 0;
        properties |= CL.CL_QUEUE_PROFILING_ENABLE;
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, properties, null);

        //endregion

        //region Memory allocation

        // Allocate the memory objects for the input- and output data
        cl_mem playerTilesMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_ulong * 2, ptrPlayerTiles, null);
        cl_mem possibleMovesMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * possibleMoves.length, ptrPossibleMoves, null);
        cl_mem randomNumbersMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * RANDOM_NUMBER_COUNT, ptrRandomNumbers, null);
        cl_mem resultsMem = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_int * NUMBER_OF_THREADS, null, null);

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
        int arg = 0;
        clSetKernelArg(kernel, arg++, Sizeof.cl_mem, Pointer.to(playerTilesMem));
        clSetKernelArg(kernel, arg++, Sizeof.cl_mem, Pointer.to(possibleMovesMem));
        clSetKernelArg(kernel, arg++, Sizeof.cl_mem, Pointer.to(randomNumbersMem));
        clSetKernelArg(kernel, arg++, Sizeof.cl_mem, Pointer.to(resultsMem));

        // Set the work-item dimensions
        long[] global_work_size = new long[]{ NUMBER_OF_THREADS };

        cl_event work_event = new cl_event();
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, null, 0, null, work_event);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, resultsMem, CL_TRUE, 0,
                Sizeof.cl_int * NUMBER_OF_THREADS, ptrResults, 0, null, null);

        // Release kernel, program and memory buffers.
        clReleaseMemObject(playerTilesMem);
        clReleaseMemObject(possibleMovesMem);
        clReleaseMemObject(randomNumbersMem);
        clReleaseMemObject(resultsMem);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // Print execution statistics.
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.addEntry("mctsKernel", work_event);
        executionStatistics.print();

        // Create arrays for the win/draw/lose counts.
        // Size is the amount of moves it could make when starting the simulation.
        int[] win_counter = new int[possibleMoves[0]];
        int[] draw_counter = new int[possibleMoves[0]];
        int[] lose_counter = new int[possibleMoves[0]];

        // Loops over the results of all simulations and depending on the ending win state
        // Increments the counter for a chosen path of Win/Draw/Lose
        for (int i = 0; i < resultArray.length; i++) {
            int chosen_path = (i % possibleMoves[0]);
            switch (resultArray[i]) {
                case 0:
                    draw_counter[chosen_path]++;
                    break;
                case 1:
                    win_counter[chosen_path]++;
                    break;
                case -1:
                    lose_counter[chosen_path]++;
                    break;
            }
        }

        // The path with the least amount of loses will be chosen.
        int smallest = 0;
        for (int i = 1; i < lose_counter.length; i++) {
            if (lose_counter[i] < lose_counter[smallest])
                smallest = i;
        }

        // Returns the path with the least amount of loses.
        return possibleMoves[smallest+1];
    }

    /**
     * This method calculates the amount of threads that can be queued based on an estimated device performance
     * and the current state of the board (the amount of tiles occupied).
     *
     * The chosen thread count is based on the following formula:
     *              y = max( sin( (-4 + x) * π) / 60 ), 0 ) + 1;
     *
     * This formula generates the following curve: https://www.desmos.com/calculator/ir56hhirju
     *
     * @param graphicsDevice The GraphicsDevice for which the amount of threads should be calculated.
     * @param board The board on which the simulation should be performed.
     * @return The amount of threads that can enqueued for this game-estimation.
     */
    private static int calculateThreadCount(GraphicsDevice graphicsDevice, GameBoard board) {
        int estimateDevicePerformance = graphicsDevice.getEstimatePerformance();
        int tileCount = board.countTilesWithState(TileState.PLAYER1) + board.countTilesWithState(TileState.PLAYER2);
        int boardSize = board.getXSize() * board.getYSize() - 4;

        double factor = Math.max(Math.sin(((-4 + tileCount) * Math.PI) / boardSize), 0d) + 1d;

        return (int) (estimateDevicePerformance * factor);
    }

    /**
     * This method tries to estimate the amount of threads / 1024 that can be calculated by the given GraphicsDevice within 10 seconds.
     * @param graphicsDevice The GraphicsDevice for which the estimates should be made.
     * @return The amount of threads / 1024 this GraphicsDevice should be able to safely perform within 10 seconds.
     */
    public static int estimateDevicePerformance(GraphicsDevice graphicsDevice) {
        int THREAD_COUNT = 250;

        // Start a counter.
        long startTest = System.currentTimeMillis();

        // Set the possible moves to the first 4 starting moves.
        int[] possibleMoves = new int[65];
        possibleMoves[0] = 4;
        possibleMoves[1] = 19;
        possibleMoves[2] = 26;
        possibleMoves[3] = 37;
        possibleMoves[4] = 44;

        // Use the boards initial state (which takes to longest to simulate).
        var player1 = Long.parseLong("0000000000000000000000000001000000001000000000000000000000000000", 2);
        var player2 = Long.parseLong("0000000000000000000000000000100000010000000000000000000000000000", 2);

        // Perform a simulation with 250 * 1024 threads.
        var move = getOptimalMoveUsingOpenCL(graphicsDevice, player1, player2, possibleMoves, THREAD_COUNT);

        // 'Stop' the counter.
        long endTest = System.currentTimeMillis();

        // Calculate the elapsed time.
        long time = endTest - startTest;

        // Try to estimate how much threads can be simulated within 10 seconds.
        return (int) (Math.floor(10000f / time) * THREAD_COUNT);
    }

    /**
     * A simple helper class for tracking cl_events and printing
     * timing information for the execution of the commands that
     * are associated with the events.
     *
     * Retrieved from here: https://github.com/gpu/JOCLSamples/blob/master/src/main/java/org/jocl/samples/JOCLEventSample.java
     * This code does not add anything to the AI except for run-time execution information in milliseconds.
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
}
