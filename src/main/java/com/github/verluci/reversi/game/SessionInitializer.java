package com.github.verluci.reversi.game;

import com.github.verluci.reversi.game.Game.*;
import com.github.verluci.reversi.game.agents.Agent;
import com.github.verluci.reversi.game.agents.LocalPlayerAgent;
import com.github.verluci.reversi.game.agents.NetworkAgent;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.clients.TelnetGameClient;

import java.security.InvalidParameterException;
import java.util.Random;

public class SessionInitializer {
    private Game game;
    private Agent player1;
    private Agent player2;

    /**
     * Constructor for SessionInitializer
     * @param player1 An Agent that is player1 in this game.
     * @param player2 An Agent that is player2 in this game.
     * @param gameType The class-definition of the game that is going to be played.
     */
    public SessionInitializer(Agent player1, Agent player2, Class<?> gameType) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = GameFactory.createGame((Class<Game>) gameType);

        player1.setGame(game);
        player2.setGame(game);

        player1.setPlayer(Player.PLAYER1);
        player2.setPlayer(Player.PLAYER2);
    }

    /**
     * Start the session in which players are playing a Game.
     * @param startingPlayer The player that is allowed to make the first move.
     */
    public void start(Agent startingPlayer) {
        if(startingPlayer == player1 || startingPlayer == player2)
            game.startGame(startingPlayer.getPlayer());
        else
            throw new InvalidParameterException("The given player is not in this session!");

        while (game.getCurrentGameState() == Game.GameState.RUNNING) {
            switch (game.getCurrentPlayer()) {
                case PLAYER1:
                    player1.performNextMove();
                    break;
                case PLAYER2:
                    player2.performNextMove();
                    break;
            }
        }
    }

    /**
     * @return The game that is being played.
     */
    public Game getGame() {
        return game;
    }

    //TODO: Remove this entry-point when all session-code has been implemented.
    /**
     * A temporary entry-point to test SessionInitializer using TicTacToe.
     * @param args Unused.
     */
    public static void main(String[] args) throws GameClientExceptions.ConnectionException, GameClientExceptions.LoginException, GameClientExceptions.SubscribeException {
        GameClient gameClient = new TelnetGameClient();
        gameClient.connect("localhost", 7789);
        String username = "player-" + new Random().nextInt(5000);
        com.github.verluci.reversi.networking.types.Player localPlayer = new com.github.verluci.reversi.networking.types.Player(username);
        gameClient.login(username);
        gameClient.subscribeToGame("Tic-tac-toe");

        Agent player1 = new LocalPlayerAgent();
        Agent player2 = new NetworkAgent(gameClient, localPlayer);

        SessionInitializer newSession = new SessionInitializer(
                player1,
                player2,
                TicTacToeGame.class);

        final com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};
        Thread sessionThread = new Thread(() -> {
            if(startingPlayer[0].equals(localPlayer))
                newSession.start(player1);
            else
                newSession.start(player2);
        });

        gameClient.onGameStart(listener -> {
            Game game = newSession.getGame();

            game.onGameEnd((winner, playerOneScore, playerTwoScore) -> {
                System.out.println("Game has ended: p1=" + playerOneScore + ", p2=" + playerTwoScore + ", winner:" + winner);
            });

            game.onGameStart(player -> {
                System.out.println("Game has started: startingPlayer=" + player);
            });

            game.onMove((mover, xPosition, yPosition) -> {
                System.out.println("Move=" + mover + " - " + xPosition + ", " + yPosition);
            });

            game.onInvalidMove((mover, xPosition, yPosition) -> {
                System.out.println("Invalid Move=" + mover + " - " + xPosition + ", " + yPosition);
            });

            game.onNextPlayer(player -> {
                System.out.println("\n" + game.getBoard().toString() + "\n");
                System.out.println("Next Player=" + player + "\n");
            });

            startingPlayer[0] = listener.getStartingPlayer();
            sessionThread.start();
        });

        gameClient.onGameEnd(listener -> {
            sessionThread.interrupt();
        });
    }
}
