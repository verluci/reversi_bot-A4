package com.github.verluci.reversi.gpgpu;

import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.TileState;
import com.github.verluci.reversi.game.agents.MCTSAIAgent;
import org.jocl.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.SplittableRandom;

import static org.jocl.CL.*;
import static org.jocl.CL.clReleaseContext;

/**
 * This class contains functions that can be used by the MCTSAIAgent
 */
public class MCTSHelper {
    private static final SplittableRandom random = new SplittableRandom();

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
     * selected path and will choose the path with the most wins.
     *
     * @param graphicsDevice The graphics-device on which the simulations should be performed.
     * @param player The player1 in the OthelloGame also known as black.
     * @param opponent The player2 in the OthelloGame also known as white.
     * @param possibleMoves An array of size 65 in which the first value is the count of possible moves, following all
     *                      possible moves. example: [ 4, 15, 13, 12, 8, 0, 0, 0, ... ]
     * @param threadCount The amount of threads x 1024 that the GraphicsDevice is going to simulate.
     * @return The most optimal move the AI was able to find.
     */
    public static int getOptimalMoveUsingOpenCL(GraphicsDevice graphicsDevice, long player, long opponent, int[] possibleMoves, int threadCount) {
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
            InputStream in = MCTSAIAgent.class.getResourceAsStream("/mcts_reversi_kernel.cl");
            content = new String(in.readAllBytes());
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

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, null, 0, null, null);

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

        // The path with the most wins will be chosen as a move.
        int largest = 0;
        for (int i = 1; i < win_counter.length; i++) {
            if (win_counter[i] > win_counter[largest])
                largest = i;
        }

        // Returns the path with the most wins.
        return possibleMoves[largest+1];
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
    public static int calculateThreadCount(GraphicsDevice graphicsDevice, GameBoard board) {
        int estimateDevicePerformance = graphicsDevice.getEstimatePerformance();
        int tileCount = board.countTilesWithState(TileState.PLAYER1) + board.countTilesWithState(TileState.PLAYER2);
        int boardSize = board.getXSize() * board.getYSize() - 4;

        double factor = Math.max(Math.sin(((-4 + tileCount) * Math.PI) / boardSize), 0d) + 1d;

        return (int) (estimateDevicePerformance * factor);
    }

    /**
     * This method tries to estimate the amount of threads / 1024 that can be calculated by the given GraphicsDevice within 10 seconds.
     * @param graphicsDevice The GraphicsDevice for which the estimates should be made.
     * @param executionTimeInSeconds The amount of time the GraphicsDevice is allowed to use to perform the simulations.
     * @return The amount of threads / 1024 this GraphicsDevice should be able to safely perform within 10 seconds.
     */
    public static int estimateDevicePerformance(GraphicsDevice graphicsDevice, float executionTimeInSeconds) {
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
        return (int) (Math.floor((executionTimeInSeconds * 1000f) / time) * THREAD_COUNT);
    }
}
