package com.github.verluci.reversi.game;

import com.github.verluci.reversi.game.events.GameEndListener;
import com.github.verluci.reversi.game.events.GameStartListener;
import com.github.verluci.reversi.game.events.MoveListener;
import com.github.verluci.reversi.game.events.TurnListener;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains an abstract definition of a two-player board-game.
 */
public abstract class Game {
    public enum Player { UNDEFINED, PLAYER1, PLAYER2 }
    public enum GameState { UNDEFINED, RUNNING, ENDED }

    private int playerOneScore;
    private int playerTwoScore;

    protected String name;
    protected GameBoard board;

    private GameState currentGameState;
    private Player currentPlayer;

    //region Listener Declaration

    private List<GameEndListener> gameEndListeners = new ArrayList<>();
    private List<GameStartListener> gameStartListeners = new ArrayList<>();
    private List<MoveListener> moveListeners = new ArrayList<>();
    private List<MoveListener> invalidMoveListeners = new ArrayList<>();
    private List<TurnListener> turnListeners = new ArrayList<>();

    //endregion

    /**
     * Constructor for Game
     * @param name The name of this game
     * @param board The board this game is played on.
     */
    public Game(String name, GameBoard board) {
        this.name = name;
        this.board = board;
        this.currentGameState = GameState.UNDEFINED;
        this.currentPlayer = Player.UNDEFINED;
    }

    //region Abstract Methods

    /**
     * This method should set all possible move tiles to the TileState POSSIBLE_MOVE
     * @param player The player for which the moves are possible.
     */
    protected abstract void findValidMoves(Player player);

    /**
     * This method should return if the given move is possible for the given player on the given position.
     * @param player The player that wants to make the move.
     * @param x The horizontal position of the move.
     * @param y The vertical position of the move.
     * @return If the move is a valid one.
     */
    public abstract boolean isValidMove(Player player, int x, int y);

    /**
     * This method performs the move on a given position for a player.
     * (This method is fired after the player knows the move is a valid one)
     * @param player The player that performs the move
     * @param x The horizontal position of the move.
     * @param y The vertical position of the move.
     */
    protected abstract void performMove(Player player, int x, int y);

    /**
     * This method is used when to board has to be reset, and should be used if you want to give the board any
     * starting stones.
     * @return A list of tiles that should appear on a new board.
     */
    protected abstract List<Tile> getStartingTiles();

    /**
     * This method checks if the game has entered the ending state.
     * (For example when to board is full, or a player lost all its tiles)
     * @return If the game has ended.
     */
    protected abstract boolean hasGameEnded();

    /**
     * This method calculates the next player given the current player and the board.
     * @param currentPlayer The current player that has played before the next player.
     * @return The next player to play.
     */
    protected abstract Player calculateNextPlayer(Player currentPlayer);

    /**
     * This method returns which player is currently leading the game.
     * @return The player that is leading the game.
     */
    protected abstract Player checkLeadingPlayer();

    //endregion

    /**
     * Will try a move if it is possible.
     * @param player The Player that is trying the move.
     * @param x The horizontal position of the tile the move is placed on.
     * @param y The vertical position of the tile the move is placed on.
     * @return If the move has been performed successful.
     */
    public boolean tryMove(Player player, int x, int y) {
        if(player.equals(currentPlayer)) {
            boolean isValidMove = isValidMove(player, x, y);

            if(isValidMove) {
                performMove(player, x, y);

                clearValidMoves();
                notifyOnMove(player, x, y);
                Player nextPlayer = calculateNextPlayer(player);

                if(nextPlayer == Player.UNDEFINED) {
                    stopGame(checkLeadingPlayer());
                    return true;
                }

                findValidMoves(nextPlayer);

                if(hasGameEnded()) {
                    Player leadingPlayer = checkLeadingPlayer();
                    stopGame(leadingPlayer);
                }
                else {
                    setCurrentPlayer(nextPlayer);
                }

                return true;
            } else {
                notifyOnInvalidMove(player, x, y);
            }
        }

        return false;
    }

    /**
     * Use this method if you want to start the game.
     * @param startingPlayer The player that should go first.
     */
    public synchronized void startGame(Player startingPlayer) {
        if(currentGameState == GameState.RUNNING)
            throw new IllegalArgumentException("The game is already running!");
        else
            currentGameState = GameState.RUNNING;

        board.empty();
        board.setTiles(getStartingTiles());

        currentPlayer = startingPlayer;
        findValidMoves(startingPlayer);
        notifyOnGameStart(startingPlayer);
    };

    /**
     * Use this method if you want to stop the game.
     * @param winner The player that should be announced winner when the game is stopped.
     */
    public synchronized void stopGame(Player winner) {
        if(currentGameState != GameState.ENDED) {
            currentGameState = GameState.ENDED;
            currentPlayer = Player.UNDEFINED;
            notifyOnGameEnd(winner);
        }
    }

    /**
     * This method clears any tiles with a non-player TileState
     */
    private void clearValidMoves(){
        Tile[][] tiles = board.getTiles();

        for (int y = 0; y < board.getYSize(); y++) {
            for (int x = 0; x < board.getXSize(); x++) {
                if(tiles[x][y].isEmpty())
                    tiles[x][y].setState(TileState.NONE);
            }
        }
    }

    //region Getters and Setters

    /**
     * @return The name of this Game.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The board the game is being played on.
     */
    public GameBoard getBoard() {
        return board;
    }

    /**
     * @return The current state of the game.
     */
    public GameState getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Use this if you want to change the current state of the game.
     * @param currentGameState The state the game should be changed to.
     */
    protected void setCurrentGameState(GameState currentGameState) {
        this.currentGameState = currentGameState;
    }

    /**
     * @return The player that is currently performing a move.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Use this method if you want to who's turn it is.
     * @param currentPlayer The player that should have the turn.
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        clearValidMoves();
        findValidMoves(currentPlayer);
        notifyOnNextPlayer(currentPlayer);
    }

    /**
     * @param player The player the score should be retrieved from.
     * @return The current score of a player.
     */
    public int getPlayerScore(Player player) {
        if(player == Player.PLAYER1)
            return playerOneScore;
        else if(player == Player.PLAYER2)
            return playerTwoScore;
        else
            throw new InvalidParameterException("A non valid player has been given!");
    };

    /**
     * Use this method if you want to change the score of a player.
     * @param player The player the score should be changed from.
     * @param score The new score this player should get.
     */
    public void setPlayerScore(Player player, int score) {
        switch (player) {
            case PLAYER1:
                playerOneScore = score;
                break;
            case PLAYER2:
                playerTwoScore = score;
                break;
            case UNDEFINED:
                throw new InvalidParameterException("A non valid player has been given!");
        }
    };

    /**
     * This method increments the score of the given player by one.
     * @param player The player the score should be incremented for.
     */
    public void incrementPlayerScore(Player player) {
        setPlayerScore(player, getPlayerScore(player) + 1);
    };

    /**
     * This method decrements the score of the given player by one.
     * @param player The player the score should be decremented for.
     */
    public void decrementPlayerScore(Player player) {
        setPlayerScore(player, getPlayerScore(player) - 1);
    };

    //endregion

    //region Events

    /**
     * Fires the given listener when a game ends.
     * @param listener The listener that should be fired when the game ends.
     */
    public void onGameEnd(GameEndListener listener) {
        gameEndListeners.add(listener);
    }

    /**
     * Fires the given listener when a game begins.
     * @param listener The listener that should be fired when a game begins.
     */
    public void onGameStart(GameStartListener listener) {
        gameStartListeners.add(listener);
    }

    /**
     * Fires the given listener when a valid move has been made.
     * @param listener The listener that should be fired when a valid move has been made.
     */
    public void onMove(MoveListener listener) {
        moveListeners.add(listener);
    }

    /**
     * Fires the given listener when an invalid move has been made.
     * @param listener The listener that should be fired when an invalid move has been made.
     */
    public void onInvalidMove(MoveListener listener) {
        invalidMoveListeners.add(listener);
    }

    /**
     * Fires the given listener when the turn has changed to a certain player.
     * (This will also fire when a player is allowed to move in a row)
     * @param listener The listener that should be fired when a turn is set for a certain player
     */
    public void onNextPlayer(TurnListener listener) {
        turnListeners.add(listener);
    }

    /**
     * Executes all listeners that have been subscribed to onGameEnd()
     * @param winner The player that has won the game.
     */
    protected void notifyOnGameEnd(Player winner) {
        for (GameEndListener listener : gameEndListeners)
            listener.onGameEnd(winner, playerOneScore, playerTwoScore);
    }

    /**
     * Executes all listeners that have been subscribed to onGameStart()
     * @param starter The player that begins.
     */
    protected void notifyOnGameStart(Player starter) {
        for (GameStartListener listener : gameStartListeners)
            listener.onGameStart(starter);
    }

    /**
     * Executes all listeners that have been subscribed to onMove()
     * @param mover The player that has made the move.
     * @param x The x position of the move.
     * @param y The y position of the move.
     */
    protected void notifyOnMove(Player mover, int x, int y) {
        for (MoveListener listener : moveListeners) {
            listener.OnMove(mover, x, y);
        }
    }

    /**
     * Executes all listeners that have been subscribed to onMove()
     * @param mover The player that has made the move.
     * @param x The x position of the move.
     * @param y The y position of the move.
     */
    protected void notifyOnInvalidMove(Player mover, int x, int y) {
        for (MoveListener listener : invalidMoveListeners) {
            listener.OnMove(mover, x, y);
        }
    }

    /**
     * Executes all listeners when the next move is allowed to be made by a player.
     * @param player The player that is allowed to make a move.
     */
    protected void notifyOnNextPlayer(Player player) {
        for (TurnListener listener : turnListeners) {
            listener.onNextPlayer(player);
        }
    }

    //endregion

    //region Static Helper Methods

    /**
     * @param player The player you want to get the opposing player's matching TileState from.
     * @return the opposing player's TileState.
     */
    protected static TileState getInvertedTileStateUsingPlayer(Player player) {
        switch (player) {
            case PLAYER1:
                return TileState.PLAYER2;
            case PLAYER2:
                return TileState.PLAYER1;
            default:
                throw new IllegalArgumentException("Only a player can be inverted!");
        }
    }

    /**
     * @param player The player the matching TileState should be retrieved from.
     * @return the player's TileState.
     */
    protected static TileState getTileStateUsingPlayer(Player player) {
        switch (player) {
            case PLAYER1:
                return TileState.PLAYER1;
            case PLAYER2:
                return TileState.PLAYER2;
            default:
                throw new IllegalArgumentException("Only a player can fetch its tile-state!");
        }
    }

    /**
     * @param player The player you want to get the opposite player for.
     * @return The opposite player of the given player.
     */
    protected static Player getOppositePlayer(Player player) {
        switch (player) {
            case PLAYER1:
                return Player.PLAYER2;
            case PLAYER2:
                return Player.PLAYER1;
        }

        throw new IllegalArgumentException("UNDINGEST not allowed");
    }

    //endregion
}
