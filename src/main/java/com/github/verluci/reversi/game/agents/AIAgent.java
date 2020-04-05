package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;

/**
 * Implement this method when you want to create an AIAgent
 * This class contains some default methods an AIAgent should have.
 */
public abstract class AIAgent extends Agent {
    /**
     * The constructor for AIAgent
     */
    public AIAgent() {
        super();
    }

    /**
     * Implement this method and make it return a certain tile on the board.
     * @param board The board on which the optimal tile should be found on.
     * @return The optimal chosen Tile.
     */
    protected abstract Tile findOptimalMove(GameBoard board);

    /**
     * A default performNextMove() method for all AI's that is  based on finding the optimal move.
     */
    @Override
    public void performNextMove() {
        Tile optimalMove = findOptimalMove(game.getBoard());
        move(optimalMove.getXCoordinate(), optimalMove.getYCoordinate());
    }
}
