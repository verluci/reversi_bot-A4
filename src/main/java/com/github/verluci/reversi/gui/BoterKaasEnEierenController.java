package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.game.agents.Agent;
import com.github.verluci.reversi.game.agents.FirstMoveAIAgent;
import com.github.verluci.reversi.game.agents.NetworkAgent;
import com.github.verluci.reversi.game.agents.RandomMoveAIAgent;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.types.Difficulty;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.io.IOException;

public class BoterKaasEnEierenController extends AnchorPane {
    private App application;
    Agent player1;
    Agent player2;
    com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};
    private com.github.verluci.reversi.networking.types.Player localPlayer;

    SessionInitializer session;

    boolean online;

    Game game;

    private GameClient gameClient;

    @FXML
    private GridPane bkepane;

    @FXML
    private Text status;

    public void setApp(App app){
        this.application = app;
    }

    public void initData(boolean online)  {

    }

    private void updateGameBoard(){
        Tile[][] tiles = game.getBoard().getTiles();
        bkepane.getChildren().retainAll(bkepane.getChildren().get(0));
        System.out.println(game.getBoard().toString());
        for(int i = 0; i < tiles.length; i++)
            for(int j = 0; j<tiles[i].length; j++) {
                if(tiles[i][j].getState() == TileState.POSSIBLE_MOVE) {
                    if(game.getCurrentPlayer().equals(Game.Player.PLAYER1)) {
                        int x = i;
                        int y = j;

                        Button button = new Button();

                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                game.tryMove(Game.Player.PLAYER1, x, y);
                                updateGameBoard();
                            }
                        });
                        bkepane.add(button, i, j);
                    }
                } else if (tiles[i][j].getState() == TileState.PLAYER1){
                    Circle circle = new Circle();
                    circle.setRadius(95);
                    circle.setStrokeWidth(95);
                    bkepane.add(circle, i, j);
                } else if (tiles[i][j].getState() == TileState.PLAYER2){
                    Line line1 = new Line();
                    line1.setStartX(5);
                    line1.setStartY(5);
                    line1.setStartX(195);
                    line1.setStartY(195);
                    line1.setStrokeWidth(5);
                    Line line2 = new Line();
                    line2.setStartX(195);
                    line2.setStartY(5);
                    line2.setEndX(5);
                    line2.setEndY(195);
                    line2.setStrokeWidth(5);
                    bkepane.add(line1, i, j);
                    bkepane.add(line2, i, j);

                }
            }
    }

    public void setupAIGame(Difficulty difficulty){
        status.setText("Jouw spel tegen de computer");
        if(difficulty == Difficulty.MAKKELIJK){
            player2 = new FirstMoveAIAgent();
        }else if (difficulty == Difficulty.NORMAAL) {
            player2 = new RandomMoveAIAgent();
        }
        session = new SessionInitializer(player1, player2, TicTacToeGame.class);
        startGame();
    }

    public void setupMultiplayerGame() {
        status.setText("Er word een spel gezocht");
        gameClient = application.getInstance().gameClient;
        localPlayer = application.getInstance().localPlayer;
        player1 = application.getInstance().player1;
        player2 = new NetworkAgent(gameClient, localPlayer);
        session = new SessionInitializer(player1, player2, TicTacToeGame.class);

        try {
            gameClient.subscribeToGame("Tic-tac-toe");
        } catch(Exception e)
        {
            System.out.println("Something went wrong");
        }
        startGame();
    }

    private void startGame(){
        Thread sessionThread = new Thread(() -> {
            if(startingPlayer[0].equals(localPlayer)) {
                session.start(player1);
            }
            else {
                session.start(player2);
            }
        });

        gameClient.onGameStart(listener -> {
            game = session.getGame();

            game.onGameEnd((winner, playerOneScore, playerTwoScore) -> {
                status.setText("Het spel is geindigd! " + winner.name() + " heeft gewonnen");
            });

            game.onGameStart(player -> {
                status.setText("Je speelt Boter Kaas en Eieren!");
                Platform.runLater(this::updateGameBoard);
            });

            game.onMove((mover, xPosition, yPosition) -> {
                Platform.runLater(this::updateGameBoard);
            });

            startingPlayer[0] = listener.getStartingPlayer();
            sessionThread.start();
        });

        gameClient.onGameEnd(listener -> {
            sessionThread.interrupt();
        });
    }
}
