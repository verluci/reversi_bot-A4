package com.github.verluci.reversi.networking;

import com.github.verluci.reversi.networking.clients.TelnetGameClient;
import com.github.verluci.reversi.networking.types.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class contains all networking related tests.
 */
public class NetworkingTest {
    //region TelnetGameClient Message Processing

    /**
     * A Test to make sure onReceiveChallenge() works as expected.
     * Creates a challenge for the processQueuedString() method to process.
     */
    @Test
    public void shouldNotifyOnReceiveChallenge() {
        // Initiate the TelnetGameClient.
        TelnetGameClient gameClient = new TelnetGameClient();

        // Initiate the challenge when information is received.
        final Challenge[] challenge = new Challenge[1];
        gameClient.onReceiveChallenge(listener -> challenge[0] = listener);

        // Send a challenge message to be processed.
        gameClient.processQueuedString("SVR GAME CHALLENGE {CHALLENGER: \"Jip\", CHALLENGENUMBER: \"1\", GAMETYPE: \"Reversi\"}");

        // Test if the processed challenge contains the following received information:
        Assert.assertEquals(new Player("Jip"), challenge[0].getPlayer());
        Assert.assertEquals(1, challenge[0].getNumber());
        Assert.assertEquals("Reversi", challenge[0].getGameType());
    }

    /**
     * A Test to make sure onCancelChallenge() works as expected.
     * Creates a cancelled challenge for the processQueuedString() method to process.
     * (This test is not completely atomic, because it expects receiving a challenge works)
     */
    @Test
    public void shouldNotifyOnCancelChallenge() {
        // Initiate the TelnetGameClient.
        TelnetGameClient gameClient = new TelnetGameClient();

        // Initiate challenge when a challenge has been cancelled (not when it has been created).
        final Challenge[] challenge = new Challenge[1];
        gameClient.onCancelChallenge(listener -> challenge[0] = listener);

        // Send a challenge message to be processed.
        gameClient.processQueuedString("SVR GAME CHALLENGE {CHALLENGER: \"Janneke\", CHALLENGENUMBER: \"1\", GAMETYPE: \"Tic-tac-toe\"}");
        gameClient.processQueuedString("SVR GAME CHALLENGE CANCELLED {CHALLENGENUMBER: \"1\"}");

        // Test if the cancelled challenge contains the following received information:
        Assert.assertEquals(new Player("Janneke"), challenge[0].getPlayer());
        Assert.assertEquals(1, challenge[0].getNumber());
        Assert.assertEquals("Tic-tac-toe", challenge[0].getGameType());
    }

    /**
     * A Test to make sure onGameEnd() works as expected.
     * Creates multiple gameEnd's for the processQueuedString() method to process.
     */
    @Test
    public void shouldNotifyOnGameEnd() {
        // Initiate the TelnetGameClient.
        TelnetGameClient gameClient = new TelnetGameClient();

        // Initiate the gameEnd when information is received.
        final GameEnd[] gameEnd = new GameEnd[1];
        gameClient.onGameEnd(listener -> gameEnd[0] = listener);

        // Send a gameEnd message to be processed.
        gameClient.processQueuedString("SVR GAME LOSS {PLAYERONESCORE: \"15\", PLAYERTWOSCORE: \"1\", COMMENT: \"MESSAGE\"}");

        // Test if the processed gameEnd contains the following received information:
        Assert.assertEquals(GameResult.LOSS, gameEnd[0].getResult());
        Assert.assertEquals(15, gameEnd[0].getPlayerOneScore());
        Assert.assertEquals(1, gameEnd[0].getPlayerTwoScore());
        Assert.assertEquals("MESSAGE", gameEnd[0].getComment());

        // Send a second gameEnd message to be processed.
        gameClient.processQueuedString("SVR GAME WIN {PLAYERONESCORE: \"12\", PLAYERTWOSCORE: \"3\", COMMENT: \"MESSAGE2\"}");

        // Test if the processed gameEnd contains the following received information:
        Assert.assertEquals(GameResult.WIN, gameEnd[0].getResult());
        Assert.assertEquals(12, gameEnd[0].getPlayerOneScore());
        Assert.assertEquals(3, gameEnd[0].getPlayerTwoScore());
        Assert.assertEquals("MESSAGE2", gameEnd[0].getComment());

        // Send a second gameEnd message to be processed.
        gameClient.processQueuedString("SVR GAME DRAW {PLAYERONESCORE: \"4\", PLAYERTWOSCORE: \"5\", COMMENT: \"\"}");

        // Test if the processed gameEnd contains the following received information:
        Assert.assertEquals(GameResult.DRAW, gameEnd[0].getResult());
        Assert.assertEquals(4, gameEnd[0].getPlayerOneScore());
        Assert.assertEquals(5, gameEnd[0].getPlayerTwoScore());
        Assert.assertEquals("", gameEnd[0].getComment());
    }

    /**
     * A Test to make sure onMove() works as expected.
     * Creates a move for the processQueuedString() method to process.
     */
    @Test
    public void shouldNotifyOnMove() {
        // Initiate the TelnetGameClient.
        TelnetGameClient gameClient = new TelnetGameClient();

        // Initiate move when information is received.
        final Move[] move = new Move[1];
        gameClient.onMove(listener -> move[0] = listener);

        // Send a move message to be processed.
        gameClient.processQueuedString("SVR GAME MOVE {PLAYER: \"Janneke\", MOVE: \"26\", DETAILS: \"Illegal move\"}");

        // Test if the processed move contains the following received information:
        Assert.assertEquals(26, move[0].getMove());
        Assert.assertEquals(new Player("Janneke"), move[0].getPlayer());
        Assert.assertEquals("Illegal move", move[0].getDetails());
    }

    /**
     * A Test to make sure onGameStart() works as expected.
     * Creates a start-message for the processQueuedString() method to process.
     */
    @Test
    public void shouldNotifyOnGameStart() {
        // Initiate the TelnetGameClient.
        TelnetGameClient gameClient = new TelnetGameClient();

        // Initiate move when information is received.
        final GameStart[] start = new GameStart[1];
        gameClient.onGameStart(listener -> start[0] = listener);

        // Send a start message to be processed.
        gameClient.processQueuedString("SVR GAME MATCH {PLAYERTOMOVE: \"Jip\", GAMETYPE: \"Tic-tac-toe\", OPPONENT: \"Janneke\"}");

        // Test if the processed start contains the following received information:
        Assert.assertEquals("Tic-tac-toe", start[0].getGameType());
        Assert.assertEquals(new Player("Janneke"), start[0].getOpponent());
        Assert.assertEquals(new Player("Jip"), start[0].getStartingPlayer());
        Assert.assertNotEquals(start[0].getStartingPlayer(), start[0].getOpponent());

        // Send a start message to be processed.
        gameClient.processQueuedString("SVR GAME MATCH {PLAYERTOMOVE: \"Janneke\", GAMETYPE: \"Reversi\", OPPONENT: \"Janneke\"}");

        // Test if the processed start contains the following received information:
        Assert.assertEquals("Reversi", start[0].getGameType());
        Assert.assertEquals(new Player("Janneke"), start[0].getOpponent());
        Assert.assertEquals(new Player("Janneke"), start[0].getStartingPlayer());
        Assert.assertEquals(start[0].getStartingPlayer(), start[0].getOpponent());
    }

    /**
     * A Test to make sure onTurn() works as expected.
     * Creates a turn-message for the processQueuedString() method to process.
     */
    @Test
    public void shouldNotifyOnReceiveTurn() {
        // Initiate the TelnetGameClient.
        TelnetGameClient gameClient = new TelnetGameClient();

        // Initiate move when information is received.
        final String[] turn = new String[1];
        gameClient.onTurn(listener -> turn[0] = listener);

        // Send a start message to be processed.
        gameClient.processQueuedString("SVR GAME YOURTURN {TURNMESSAGE: \"MESSAGE\"}");

        // Check if the received message has been processed.
        Assert.assertEquals("MESSAGE", turn[0]);
    }

    //endregion
}
