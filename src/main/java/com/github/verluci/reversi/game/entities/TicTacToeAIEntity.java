package com.github.verluci.reversi.game.entities;

import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.game.GameBoard;
import com.github.verluci.reversi.game.Tile;
import com.github.verluci.reversi.game.TileState;

import java.security.InvalidParameterException;

/**
 * Use this Entity if you want a player to play as a TicTacToe AI.
 */
public class TicTacToeAIEntity extends AIEntity {
    /**
     * The constructor for TicTacToeAIEntity
     */
    public TicTacToeAIEntity() {
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
        if(!game.getName().equals("Tic-tac-toe"))
            throw new InvalidParameterException("This AI Entity only works on 'Tic-tac-toe'!");

        super.setGame(game);
    }
}
