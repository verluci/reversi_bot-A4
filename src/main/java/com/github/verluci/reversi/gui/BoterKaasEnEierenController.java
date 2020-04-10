package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.game.SessionInitializer;
import com.github.verluci.reversi.game.TicTacToeGame;
import com.github.verluci.reversi.game.agents.Agent;
import com.github.verluci.reversi.game.agents.NetworkAgent;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.GameClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class BoterKaasEnEierenController extends AnchorPane {
    private App application;
    Agent player1;
    Agent player2;
    com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};
    private com.github.verluci.reversi.networking.types.Player localPlayer;

    SessionInitializer session;

    private GameClient gameClient;

    @FXML
    private Label beurt;

    public void setApp(App app){
        this.application = app;
    }

    public BoterKaasEnEierenController() throws GameClientExceptions.SubscribeException {
        System.out.println("here");
        gameClient = application.getInstance().gameClient;
        gameClient.subscribeToGame("Tic-tac-toe");
        localPlayer = application.getInstance().localPlayer;
        player1 = application.getInstance().player1;
        player2 = new NetworkAgent(gameClient, localPlayer);
        session = new SessionInitializer(player1, player2, TicTacToeGame.class);
    }

    public void initialize() {
        System.out.println("initialize");
        Thread sessionThread = new Thread(() -> {
            if(startingPlayer[0].equals(localPlayer))
                session.start(player1);
            else
                session.start(player2);
        });
        gameClient.onGameStart(listener -> {
            Game game = session.getGame();

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
