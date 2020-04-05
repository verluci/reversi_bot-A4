package com.github.verluci.reversi.game.agents;

import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;
import com.github.verluci.reversi.game.TileState;

/**
 * Use this agent if you want to play against an AI that will always pick the first possible tile.
 */
public class FirstMoveAIAgent extends AIAgent {
    /**
     * The constructor for TicTacToeAIEntity
     */
    public FirstMoveAIAgent() {
        super();
    }

    /**
     * @param board The board on which the optimal tile should be found on.
     * @return The first possible move it can find.
     */
    @Override
    protected Tile findOptimalMove(GameBoard board) {
        var possibleMoves = board.getTilesWithState(TileState.POSSIBLE_MOVE);
        return possibleMoves.get(0);
    }

    /**
     * Sets the game this entity should play in.
     * @param game The game this entity should play in.
     */
    @Override
    public void setGame(Game game) {
        super.setGame(game);
    }
}
