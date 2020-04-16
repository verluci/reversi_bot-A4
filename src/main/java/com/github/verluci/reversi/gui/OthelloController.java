package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.game.agents.*;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.types.Difficulty;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;


public class OthelloController extends AnchorPane {
    private App application;
    LocalUIPlayerAgent player1;
    Agent player2;
    private com.github.verluci.reversi.networking.types.Player localPlayer;
    Thread sessionThread;

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

    @FXML
    private Button exitButton;

    public void initialize() {
        BackgroundImage backgroundImage = new BackgroundImage(new Image("/hout.jpg"), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT );
        othpane.setBackground(new Background(backgroundImage));
        localPlayer = application.getInstance().localPlayer;
        player1 = new LocalUIPlayerAgent();
        exitButton.setVisible(false);
    }

    private void updateGameBoard(){
        int witScore = 0;
        int zwartScore = 0;

        Tile[][] tiles = game.getBoard().getTiles();

        othpane.getChildren().retainAll(othpane.getChildren().get(0));

        for(int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j].getState() == TileState.POSSIBLE_MOVE) {
                    if (game.getCurrentPlayer().equals(player1.getPlayer())) {
                        int x = i;
                        int y = j;

                        Circle circle = new Circle();
                        circle.setRadius(35);
                        circle.setStrokeWidth(10);
                        circle.setFill(Color.WHITE);
                        circle.setOpacity(0.3);

                        circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                            player1.doMove( x, y);
                            updateGameBoard();
                        });

                        othpane.add(circle, i, j);
                    }
                } else if (tiles[i][j].getState() == TileState.PLAYER1) {
                    witScore++;
                    Image image = new Image("/othello_wit.png");

                    ImageView imageView = new ImageView();
                    imageView.setImage(image);
                    imageView.setFitHeight(70);
                    imageView.setPreserveRatio(true);
                    imageView.setCache(true);

                    othpane.add(imageView, i, j);
                } else if (tiles[i][j].getState() == TileState.PLAYER2) {
                    zwartScore++;
                    Image image = new Image("/othello_zwart.png");

                    ImageView imageView = new ImageView();
                    imageView.setImage(image);
                    imageView.setFitHeight(70);
                    imageView.setPreserveRatio(true);
                    imageView.setCache(true);
                    othpane.add(imageView, i, j);
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
        sessionThread = new Thread(() -> {
            session.start(player1);
        });
        startGame();
    }

    public void setupMultiplayerGame(){
        status.setText("Er word een spel gezocht");
        player2 = new NetworkAgent(gameClient, localPlayer);
        session = new SessionInitializer(player1, player2, OthelloGame.class);
        com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};

        try {
            gameClient.subscribeToGame("Reversi");
        }catch(Exception e){
            System.out.println(e);
        }

        sessionThread = new Thread(() -> {
            if(startingPlayer[0].equals(localPlayer)) {
                session.start(player1);
            }
            else {
                session.start(player2);
            }
        });

        gameClient.onGameStart(listener -> {
            startingPlayer[0] = listener.getStartingPlayer();
            startGame();
        });

        gameClient.onGameEnd(listener -> {
            sessionThread.interrupt();
        });
    }

    private void startGame(){
        wit.setText("Wit: 0");
        zwart.setText("Zwart: 0");

        game = session.getGame();

        game.onGameEnd((winner, playerOneScore, playerTwoScore) -> {
            status.setText("Het spel is geindigd! " + winner.name() + " heeft gewonnen");
        });

        game.onGameStart(player -> {
            status.setText("Je speelt Othello!");
            exitButton.setVisible(true);
            Platform.runLater(this::updateGameBoard);
        });

        game.onNextPlayer(player -> {
            System.out.println("next");
            Platform.runLater(this::updateGameBoard);
        });

        sessionThread.start();
    }

    public void exit(ActionEvent actionEvent) {
        try {
            gameClient.forfeit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            application.getInstance().navigateScene("lobby");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


