package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;
import com.github.verluci.reversi.game.TileState;

/**
 * Use this agent if you want to play against an AI that will always pick the first possible tile.
 */
public class FirstMoveAIAgent extends AIAgent {
    /**
     * @param board The board on which the optimal tile should be found on.
     * @return The first possible move it can find.
     */
    @Override
    protected Tile findOptimalMove(GameBoard board) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var possibleMoves = board.getTilesWithState(TileState.POSSIBLE_MOVE);
        return possibleMoves.get(0);
    }
}
