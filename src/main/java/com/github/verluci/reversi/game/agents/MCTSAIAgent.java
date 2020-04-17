package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.gpgpu.GraphicsDevice;
import com.github.verluci.reversi.gpgpu.MCTSHelper;

/**
 * This class contains a GPU accelerated Othello AI based on the MCTS algorithm.
 * https://en.wikipedia.org/wiki/Monte_Carlo_tree_search
 *
 * For the explanation of the data allocation process see the function MCTSHelper.getOptimalMoveUsingOpenCL()
 * in the package com.github.verluci.reversi.gpgpu;
 *
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
        int threadCount = MCTSHelper.calculateThreadCount(graphicsDevice, board);

        // Estimate the most optimal move with OpenCL using the provided GraphicsDevice and threadCount.
        int move = MCTSHelper.getOptimalMoveUsingOpenCL(graphicsDevice, player1, player2, possibleMoves, threadCount);

        // Convert the retrieved optimal tile-index to an x and y coordinate
        int x = move % board.getXSize();
        int y = move / board.getXSize();

        // Return the estimated optimal move.
        return board.getTile(x, y);
    }

    /**
     * setGame() is overriden in MCTSAIAgent because the MCTSAIAgent only works for OthelloGame.
     * @param game The game this agent should play in.
     */
    @Override
    public void setGame(Game game) {
        if(game instanceof OthelloGame)
            super.setGame(game);
        else
            throw new IllegalArgumentException("This MCTS-AI can only be used for Othello/Reversi!");
    }
}
