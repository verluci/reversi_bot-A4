package com.github.lutzenh.networking;

import com.github.lutzenh.networking.GameClientExceptions.*;

public interface GameClient {
    void connect(String hostname, int port)                      throws ConnectionException;
    void disconnect()                                            throws ConnectionException;
    boolean getConnected();

    void login(String username)                                  throws LoginException;
    void logout()                                                throws LoginException;

    String[] getGameList();
    Player[] getPlayerList();
    Challenge[] getChallenges();

    void subscribeToGame(String gameName)                        throws SubscribeException;
    void challengePlayer(Player player, String gameName)        throws ChallengePlayerException;
    void acceptChallenge(Challenge challenge)                    throws ChallengePlayerException;

    void performMove(int position)                               throws MoveException;
    void forfeit()                                               throws MoveException;
}
