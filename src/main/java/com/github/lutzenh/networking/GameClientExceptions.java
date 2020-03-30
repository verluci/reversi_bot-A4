package com.github.lutzenh.networking;

public class GameClientExceptions {
    public static class ChallengePlayerException extends Exception
        { public ChallengePlayerException(String message) { super(message); }}

    public static class ConnectionException extends Exception
        { public ConnectionException(String message) { super(message); }}

    public static class LoginException extends Exception
        { public LoginException(String message) { super(message); }}

    public static class MoveException extends Exception
        { public MoveException(String message) { super(message); }}

    public static class SubscribeException extends Exception
        { public SubscribeException(String message) { super(message); }}
}


