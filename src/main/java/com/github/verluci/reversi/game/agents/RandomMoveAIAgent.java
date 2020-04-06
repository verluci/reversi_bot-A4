package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;
import com.github.verluci.reversi.game.TileState;

import java.util.Random;

/**
 * Use this agent if you want to play against an AI that will always pick a random tile.
 */
public class RandomMoveAIAgent extends AIAgent {

    private Random random;

    /**
     * Constructor for RandomMoveAIAgent
     */
    public RandomMoveAIAgent() {
        super();
        random = new Random();
    }

    /**
     * @param board The board on which the optimal tile should be found on.
     * @return A random move on the board.
     */
    @Override
    protected Tile findOptimalMove(GameBoard board) {
        var possibleMoves = board.getTilesWithState(TileState.POSSIBLE_MOVE);
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }
}
