package com.github.verluci.reversi.game;

import com.github.verluci.reversi.game.Game.*;
import com.github.verluci.reversi.game.agents.*;
import com.github.verluci.reversi.gpgpu.JOCLSample;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.clients.TelnetGameClient;

import java.security.InvalidParameterException;
import org.apache.commons.cli.*;

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

    /**
     * An entry-point which can be used to run the Othello/Reversi AI in a headless state.
     * @param args -h HOSTNAME, -p PORT, -u USERNAME
     */
    public static void main(String[] args) throws GameClientExceptions.ConnectionException, GameClientExceptions.LoginException {
        //region Command Line Arguments

        Options options = new Options();

        Option usernameOption = new Option("u", "username", true, "The player's username on the server.");
        usernameOption.setRequired(true);
        options.addOption(usernameOption);

        Option hostnameOption = new Option("h", "hostname", true, "The host-name or ip-address of the server.");
        hostnameOption.setRequired(true);
        options.addOption(hostnameOption);

        Option portOption = new Option("p", "port", true, "The port of the server.");
        portOption.setRequired(true);
        options.addOption(portOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(" ", options);

            System.exit(1);
        }

        String username = cmd.getOptionValue("username");
        String hostname = cmd.getOptionValue("hostname");
        int port = Integer.parseInt(cmd.getOptionValue("port"));

        //endregion

        var graphicsDevices = JOCLSample.getGraphicsDevices();
        var chosenDevice = graphicsDevices.get(0);

        GameClient gameClient = new TelnetGameClient();
        gameClient.connect(hostname, port);

        com.github.verluci.reversi.networking.types.Player localPlayer = new com.github.verluci.reversi.networking.types.Player(username);
        gameClient.login(username);

        gameClient.onGameStart(listener -> {
            Agent player1 = new MCTSAIAgent(chosenDevice);
            Agent player2 = new NetworkAgent(gameClient, localPlayer);

            SessionInitializer newSession;

            System.out.println("Starting player is " + listener.getStartingPlayer().getName());
            System.out.println("The local Agent is " + (listener.getStartingPlayer().getName().equals(username) ? "PLAYER1" : "PLAYER2"));

            if(listener.getStartingPlayer().getName().equals(username))
                newSession = new SessionInitializer(
                        player1,
                        player2,
                        OthelloGame.class);
            else
                newSession = new SessionInitializer(
                        player2,
                        player1,
                        OthelloGame.class);

            final com.github.verluci.reversi.networking.types.Player[] startingPlayer = { null };
            SessionInitializer finalNewSession = newSession;
            Thread sessionThread = new Thread(() -> {
                if(startingPlayer[0].equals(localPlayer))
                    finalNewSession.start(player1);
                else
                    finalNewSession.start(player2);
            });

            Game game = newSession.getGame();

            game.onGameEnd((winner, playerOneScore, playerTwoScore) -> {
                System.out.println("Game has ended: p1=" + playerOneScore + ", p2=" + playerTwoScore + ", winner:" + winner);
                System.out.println("\n" + game.getBoard().toString() + "\n");
            });

            startingPlayer[0] = listener.getStartingPlayer();
            sessionThread.start();
        });
    }
}
