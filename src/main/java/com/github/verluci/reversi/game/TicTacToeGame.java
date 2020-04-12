package com.github.verluci.reversi.game;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an implementation of the game Tic-tac-toe
 */
public class TicTacToeGame extends Game {
    // The size of a TicTacToe game.
    private static final int BOARD_SIZE = 3;

    /**
     * The constructor for TicTacToeGame
     */
    public TicTacToeGame() {
        super("Tic-tac-toe", new GameBoard(BOARD_SIZE, BOARD_SIZE));
    }

    @Override
    protected void findValidMoves(Player player) {
        Tile[][] tiles = board.getTiles();

        for (int y = 0; y < board.getYSize(); y++) {
            for (int x = 0; x < board.getXSize(); x++) {
                if(tiles[x][y].isEmpty())
                    tiles[x][y].setState(TileState.POSSIBLE_MOVE);
            }
        }
    }

    @Override
    public boolean isValidMove(Player player, int x, int y) {
        if(player == Player.UNDEFINED)
            return false;

        Tile tile = board.getTile(x, y);
        return tile.isEmpty();
    }

    @Override
    protected void performMove(Player player, int x, int y) {
        Tile tile = board.getTile(x, y);
        tile.setState(getTileStateUsingPlayer(player));
    }

    @Override
    protected Player calculateNextPlayer(Player currentPlayer) {
        switch (currentPlayer) {
            case PLAYER1:
                return Player.PLAYER2;
            case PLAYER2:
                return Player.PLAYER1;
        }

        throw new InvalidParameterException("The current player can't be undefined!");
    }

    @Override
    protected Player checkLeadingPlayer() {
        if(getPlayerScore(Player.PLAYER1) == getPlayerScore(Player.PLAYER2))
            return Player.UNDEFINED;
        else
            return getPlayerScore(Player.PLAYER1) > getPlayerScore(Player.PLAYER2) ? Player.PLAYER1 : Player.PLAYER2;
    }

    @Override
    protected boolean hasGameEnded() {
        switch (getCurrentPlayer()) {
            case PLAYER1:
                if(checkRowOfThree(TileState.PLAYER1)) {
                    incrementPlayerScore(Player.PLAYER1);
                    return true;
                }
            case PLAYER2:
                if(checkRowOfThree(TileState.PLAYER2)) {
                    incrementPlayerScore(Player.PLAYER2);
                    return true;
                }
        }

        return board.getTilesWithState(TileState.NONE).isEmpty() && board.getTilesWithState(TileState.POSSIBLE_MOVE).isEmpty();
    }

    /**
     * This method checks if a row of three exists with the given player's TileState.
     * @param tileStatePlayer The TileState of the player that should be checked.
     * @return If a row of three exists with the given player's TileState.
     */
    private boolean checkRowOfThree(TileState tileStatePlayer) {
        Tile[][] tiles = board.getTiles();

        // Check all horizontal and all vertical rows.
        for (int i = 0; i < BOARD_SIZE; i++) {
            if(tiles[0][i].getState() == tileStatePlayer
                    && tiles[1][i].getState() == tileStatePlayer
                    && tiles[2][i].getState() == tileStatePlayer)
                return true;

            if(tiles[i][0].getState() == tileStatePlayer
                    && tiles[i][1].getState() == tileStatePlayer
                    && tiles[i][2].getState() == tileStatePlayer)
                return true;
        }

        // Check diagonal \
        if(tiles[0][0].getState() == tileStatePlayer
                && tiles[1][1].getState() == tileStatePlayer
                && tiles[2][2].getState() == tileStatePlayer)
            return true;

        // Check diagonal /
        if(tiles[2][0].getState() == tileStatePlayer
                && tiles[1][1].getState() == tileStatePlayer
                && tiles[0][2].getState() == tileStatePlayer)
            return true;

        return false;
    }

    /**
     * @return An empty list, (TicTacToe does not have any starting tiles)
     */
    @Override
    public List<Tile> getStartingTiles() {
        return new ArrayList<>();
    }
}
