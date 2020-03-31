package com.github.lutzenh.networking;

import com.github.lutzenh.networking.GameClientExceptions.*;

/**
 * This interface can be inherited by different connection types
 * (for example using a telnet connection, or an http connection).
 */
public interface GameClient {

    /**
     * Use this method to connect to a given telnet-server
     * @param hostname The hostname of this server
     * @param port The port of this server. (usually 7789)
     * @throws ConnectionException Thrown when a connection to the server fails.
     */
    void connect(String hostname, int port) throws ConnectionException;

    /**
     * Disconnects you from the server.
     * @throws ConnectionException Thrown when disconnecting from the server fails.
     */
    void disconnect() throws ConnectionException;

    /**
     * @return True if you are currently connected to the server.
     */
    boolean getConnected();

    /**
     * Use this method if you want to login to the server with a certain username.
     * @param username the unique username you want to use to connect to the server.
     * @throws LoginException Thrown when the username already exists or if the command has failed.
     */
    void login(String username) throws LoginException;

    /**
     * Use this command if you want to logout.
     * @throws LoginException Thrown when logging out somehow fails.
     */
    void logout() throws LoginException;

    /**
     * Retrieves a list of games that the server supports.
     * @return A String[] array of games that the server supports.
     */
    String[] getGameList();

    /**
     * Retrieves all the players that are currently online.
     * @return A list of players that are online.
     */
    Player[] getPlayerList();

    /**
     * @return A list of challenges against you.
     */
    Challenge[] getChallenges();

    /**
     * Use this method if you want to subscribe to a game inorder to auto-join a match.
     * @param gameName The name of the game you want to subscribe to.
     * @throws SubscribeException Thrown when the response is has failed.
     */
    void subscribeToGame(String gameName) throws SubscribeException;

    /**
     * Use this method if you want to challenge a player to a match.
     * @param player The player you want to challenge.
     * @param gameName The name of the game you want to play with the challenged player.
     * @throws ChallengePlayerException Thrown when challenging a player fails because of invalid Player-name or Game-name.
     */
    void challengePlayer(Player player, String gameName) throws ChallengePlayerException;

    /**
     * Use this method when you want to accept a given challenge.
     * @param challenge The challenge that should be accepted.
     * @throws ChallengePlayerException Thrown when accepting a challenge fails.
     */
    void acceptChallenge(Challenge challenge) throws ChallengePlayerException;

    /**
     * Performs a move on the given position. beware! will return OK on illegal moves!
     * @param position The position the move should be performed on.
     * @throws MoveException Thrown when a move fails because of invalid syntax (not because of illegal moves).
     */
    void performMove(int position) throws MoveException;

    /**
     * Use this method when you want to give up during a match.
     * @throws MoveException Thrown when the response has failed.
     */
    void forfeit() throws MoveException;
}
