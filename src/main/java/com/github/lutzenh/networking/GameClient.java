package com.github.lutzenh.networking;

import com.github.lutzenh.networking.GameClientExceptions.*;
import com.github.lutzenh.networking.listeners.GameEndListener;
import com.github.lutzenh.networking.listeners.GameStartListener;
import com.github.lutzenh.networking.listeners.MoveListener;
import com.github.lutzenh.networking.listeners.TurnListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This interface can be inherited by different connection types
 * (for example using a telnet connection, or an http connection).
 */
public abstract class GameClient {

    //region Listener Declaration

    private List<GameEndListener> gameEndListeners = new ArrayList<>();
    private List<GameStartListener> gameStartListeners = new ArrayList<>();
    private List<MoveListener> moveListeners = new ArrayList<>();
    private List<TurnListener> turnListeners = new ArrayList<>();

    //endregion

    //region Actions

    /**
     * Use this method to connect to a given telnet-server
     * @param hostname The hostname of this server
     * @param port The port of this server. (usually 7789)
     * @throws ConnectionException Thrown when a connection to the server fails.
     */
    abstract void connect(String hostname, int port) throws ConnectionException;

    /**
     * Disconnects you from the server.
     * @throws ConnectionException Thrown when disconnecting from the server fails.
     */
    abstract void disconnect() throws ConnectionException;

    /**
     * @return True if you are currently connected to the server.
     */
    abstract boolean getConnected();

    /**
     * Use this method if you want to login to the server with a certain username.
     * @param username the unique username you want to use to connect to the server.
     * @throws LoginException Thrown when the username already exists or if the command has failed.
     */
    abstract void login(String username) throws LoginException;

    /**
     * Use this command if you want to logout.
     * @throws LoginException Thrown when logging out somehow fails.
     */
    abstract void logout() throws LoginException;

    /**
     * Retrieves a list of games that the server supports.
     * @return A String[] array of games that the server supports.
     */
    abstract String[] getGameList();

    /**
     * Retrieves all the players that are currently online.
     * @return A list of players that are online.
     */
    abstract Player[] getPlayerList();

    /**
     * @return A list of challenges against you.
     */
    abstract Challenge[] getChallenges();

    /**
     * Use this method if you want to subscribe to a game inorder to auto-join a match.
     * @param gameName The name of the game you want to subscribe to.
     * @throws SubscribeException Thrown when the response is has failed.
     */
    abstract void subscribeToGame(String gameName) throws SubscribeException;

    /**
     * Use this method if you want to challenge a player to a match.
     * @param player The player you want to challenge.
     * @param gameName The name of the game you want to play with the challenged player.
     * @throws ChallengePlayerException Thrown when challenging a player fails because of invalid Player-name or Game-name.
     */
    abstract void challengePlayer(Player player, String gameName) throws ChallengePlayerException;

    /**
     * Use this method when you want to accept a given challenge.
     * @param challenge The challenge that should be accepted.
     * @throws ChallengePlayerException Thrown when accepting a challenge fails.
     */
    abstract void acceptChallenge(Challenge challenge) throws ChallengePlayerException;

    /**
     * Performs a move on the given position. beware! will return OK on illegal moves!
     * @param position The position the move should be performed on.
     * @throws MoveException Thrown when a move fails because of invalid syntax (not because of illegal moves).
     */
    abstract void performMove(int position) throws MoveException;

    /**
     * Use this method when you want to give up during a match.
     * @throws MoveException Thrown when the response has failed.
     */
    abstract void forfeit() throws MoveException;

    //endregion

    //region Reactions/Events

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
     * Fires the given listener when a move has been set.
     * @param listener The listener that should be fired when a move has been set.
     */
    public void onMove(MoveListener listener) {
        moveListeners.add(listener);
    }

    /**
     * Fires the given listener when the turn has been given to the player
     * @param listener The listener that should be fired when the turn is given to the player.
     */
    public void onTurn(TurnListener listener) {
        turnListeners.add(listener);
    }

    /**
     * Executes all listeners that have been subscribed to onGameEnd()
     * @param gameEnd Information about how the game has ended.
     */
    protected void notifyOnGameEnd(GameEnd gameEnd) {
        for (GameEndListener listener : gameEndListeners) {
            listener.onGameEnded(gameEnd);
        }
    }

    /**
     * Executes all listeners that have been subscribed to onGameStart()
     * @param gameStart Information about the start of the game.
     */
    protected void notifyOnGameStart(GameStart gameStart) {
        for (GameStartListener listener : gameStartListeners) {
            listener.onStartGame(gameStart);
        }
    }

    /**
     * Executes all listeners that have been subscribed to onMove()
     * @param move The move that has been made and will be sent to all listeners.
     */
    protected void notifyOnMove(Move move) {
        for (MoveListener listener : moveListeners) {
            listener.onPlayerMove(move);
        }
    }

    /**
     * Executes all listeners that have been subscribed to onTurn()
     * @param message An optional non-null message that can be sent when the player receives the turn.
     */
    protected void notifyOnTurn(String message) {
        for (TurnListener listener : turnListeners) {
            listener.onReceiveTurn(message);
        }
    }

    //endregion
}
