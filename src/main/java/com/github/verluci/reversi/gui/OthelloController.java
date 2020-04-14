package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.game.agents.*;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.types.Difficulty;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import static com.github.verluci.reversi.game.Game.Player.PLAYER1;
import static com.github.verluci.reversi.game.Game.Player.PLAYER2;

public class OthelloController extends AnchorPane {
    private App application;
    Agent player1;
    Agent player2;
    com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};
    private com.github.verluci.reversi.networking.types.Player localPlayer;

    SessionInitializer session;

    Game game;

    private GameClient gameClient;

    @FXML
    private GridPane othpane;

    @FXML
    private Text status;

    @FXML
    private Text wit;

    @FXML
    private Text zwart;

    public void setApp(App app){
        this.application = app;
    }
    private void updateGameBoard(){
        int witScore = 0;
        int zwartScore = 0;

        Tile[][] tiles = game.getBoard().getTiles();
        othpane.getChildren().retainAll(othpane.getChildren().get(0));
        System.out.println(game.getBoard().toString());
        for(int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j].getState() == TileState.POSSIBLE_MOVE) {
                    if (game.getCurrentPlayer().equals(PLAYER1)) {
                        int x = i;
                        int y = j;

                        Button button = new Button();

                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                game.tryMove(PLAYER1, x, y);
                                updateGameBoard();
                            }
                        });
                        othpane.add(button, i, j);
                    }
                } else if (tiles[i][j].getState() == TileState.PLAYER1) {
                    witScore++;
                    Circle circle = new Circle();
                    circle.setRadius(20);
                    circle.setStrokeWidth(10);
                    circle.setFill(Color.WHITE);
                    othpane.add(circle, i, j);
                } else if (tiles[i][j].getState() == TileState.PLAYER2) {
                    zwartScore++;
                    Circle circle = new Circle();
                    circle.setRadius(20);
                    circle.setStrokeWidth(10);
                    othpane.add(circle, i, j);
                }
            }
        }
        wit.setText("Wit: " + witScore);
        zwart.setText("Zwart: " + zwartScore);
    }

    public void setupAIGame(Difficulty difficulty){
        if(difficulty == Difficulty.MAKKELIJK){
            player2 = new FirstMoveAIAgent();
        }else if (difficulty == Difficulty.NORMAAL) {
            player2 = new RandomMoveAIAgent();
        }else if (difficulty == Difficulty.MOEILIJK){
            //player2 = new MCTSAIAgent();
        }
        session = new SessionInitializer(player1, player2, OthelloGame.class);
        startGame();
    }

    public void setupMultiplayerGame(){
        status.setText("Er word een spel gezocht");
        player2 = new NetworkAgent(gameClient, localPlayer);
        session = new SessionInitializer(player1, player2, OthelloGame.class);
        try {
            gameClient.subscribeToGame("Reversi");
        }catch(Exception e){
            System.out.println(e);
        }
        startGame();
    }

    private void startGame(){
        wit.setText("Wit: 0");
        zwart.setText("Zwart: 0");
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
                status.setText("Je speelt Othello!");
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

    public void initialize() throws GameClientExceptions.SubscribeException {
        othpane.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        gameClient = application.getInstance().gameClient;
        localPlayer = application.getInstance().localPlayer;
        player1 = application.getInstance().player1;
    }
}


