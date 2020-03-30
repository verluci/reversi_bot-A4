package com.github.lutzenh.networking;

import com.github.lutzenh.networking.GameClientExceptions.*;

public interface GameClient {

    public void connect(String hostname, int port)                      throws ConnectionException;
    public void disconnect()                                            throws ConnectionException;
    public boolean getConnected();

    public void login(String username)                                  throws LoginException;
    public void logout()                                                throws LoginException;

    public String[] getGameList();
    public Player[] getPlayerList();

    public void subscribeToGame(String gameName)                        throws SubscribeException;
    public void challengePlayer(String playerName, String gameName)     throws ChallengePlayerException;
    public void acceptChallenge(int challengeId)                        throws ChallengePlayerException;

    public void performMove(int position)                               throws MoveException;
    public void forfeit()                                               throws MoveException;
}
