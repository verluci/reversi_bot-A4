package com.github.lutzenh.networking;

/**
 * A Class that holds all GameClient related exceptions.
 */
public class GameClientExceptions {
    /**
     * This Exception is thrown when any action related to challenging has failed.
     */
    public static class ChallengePlayerException extends Exception
        { public ChallengePlayerException(String message) { super(message); }}

    /**
     * This Exception is thrown when any connection method has failed.
     */
    public static class ConnectionException extends Exception
        { public ConnectionException(String message) { super(message); }}

    /**
     * This Exception is thrown when a player fails to login or out.
     */
    public static class LoginException extends Exception
        { public LoginException(String message) { super(message); }}

    /**
     * This Exception is thrown when a move has failed. (illegal moves are not defined as failure).
     */
    public static class MoveException extends Exception
        { public MoveException(String message) { super(message); }}

    /**
     * This Exception is thrown when subscribing to a type of game has failed.
     */
    public static class SubscribeException extends Exception
        { public SubscribeException(String message) { super(message); }}
}


