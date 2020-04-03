package com.github.verluci.reversi.game.entities;

import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;

/**
 * Implement this method when you want to create an AIEntity
 * This class contains some default methods an AIEntity should have.
 */
public abstract class AIEntity extends Entity {
    /**
     * The constructor for AIEntity
     */
    public AIEntity() {
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
