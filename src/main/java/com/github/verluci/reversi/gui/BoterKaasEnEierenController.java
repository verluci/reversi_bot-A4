package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.game.agents.*;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.types.Difficulty;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.io.IOException;

/*
 * The scene for playing Boter kaas en eieren
 */
public class BoterKaasEnEierenController extends AnchorPane {
    LocalUIPlayerAgent player1;
    Agent player2;
    com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};
    SessionInitializer session;
    Thread sessionThread;
    Game game;
    private com.github.verluci.reversi.networking.types.Player localPlayer;
    private GameClient gameClient;

    @FXML private GridPane bkepane;
    @FXML private Text status;
    @FXML private Button exitButton;

    /*
     *  Method to initialize this scene UI.
     *  Should not be called manually, done by JavaFX
     */
    public void initialize() {
        exitButton.setVisible(false);
        gameClient = App.getInstance().getGameClient();
        localPlayer = App.getInstance().getLocalPlayer();
    }

    /*
     *  Method to update the gameboard.
     *  Sets all pieces to the right state
     */
    private void updateGameBoard() {
        Tile[][] tiles = game.getBoard().getTiles();
        bkepane.getChildren().retainAll(bkepane.getChildren().get(0));
        for (int i = 0; i < tiles.length; i++)
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j].getState() == TileState.POSSIBLE_MOVE) {
                    if (game.getCurrentPlayer().equals(Game.Player.PLAYER1)) {
                        Circle possibleMovePiece = possibleMovePiece(i, j);
                        bkepane.add(possibleMovePiece, i, j);
                    }
                } else if (tiles[i][j].getState() == TileState.PLAYER1) {
                    Circle circle = createCircle();
                    bkepane.add(circle, i, j);
                } else if (tiles[i][j].getState() == TileState.PLAYER2) {
                    Group cross = createCross();
                    bkepane.add(cross, i, j);
                }
            }
    }

    /*
     *  Method to setup a game VS AI. Sets up AI of requested difficulty and starts game.
     *  @param  difficulty  The difficulty that the player wants to play the game on
     */
    public void setupAIGame(Difficulty difficulty) {
        status.setText("Jouw spel tegen de computer");
        player1 = new LocalUIPlayerAgent();
        if (difficulty == Difficulty.MAKKELIJK) {
            player2 = new FirstMoveAIAgent();
        } else {
            player2 = new RandomMoveAIAgent();
        }
        session = new SessionInitializer(player1, player2, TicTacToeGame.class);
        sessionThread = new Thread(() -> {
            session.start(player1);
        });
        startGame();
    }

    /*
     *  Method to set up multiplayer game and start it once a game is found
     */
    public void setupMultiplayerGame() {
        status.setText("Er word een spel gezocht");
        player1 = new LocalUIPlayerAgent();
        player2 = new NetworkAgent(gameClient, localPlayer);
        session = new SessionInitializer(player1, player2, TicTacToeGame.class);

        try {
            gameClient.subscribeToGame("Tic-tac-toe");
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }

        sessionThread = new Thread(() -> {
            if (startingPlayer[0].equals(localPlayer)) {
                session.start(player1);
            } else {
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

    /*
     *  Method to start the game when everything is set up
     */
    private void startGame() {
        game = session.getGame();

        game.onGameEnd((winner, playerOneScore, playerTwoScore) -> {
            status.setText("Het spel is geindigd! " + winner.name() + " heeft gewonnen");
        });

        game.onGameStart(player -> {
            status.setText("Je speelt Boter Kaas en Eieren!");
            exitButton.setVisible(true);
            Platform.runLater(this::updateGameBoard);
        });

        game.onNextPlayer(player -> {
            Platform.runLater(this::updateGameBoard);
        });

        game.onMove((mover, xPosition, yPosition) -> {
            Platform.runLater(this::updateGameBoard);
        });

        sessionThread.start();
    }

    /*
     *  Method that is called by FXML Button to exit scene and return to lobby
     *  @param actionEvent  Buttons ActionEvent
     */
    public void exit(ActionEvent actionEvent) {
        try {
            if (gameClient != null) {
                gameClient.forfeit();
            }
        } catch (Exception e) {

        }
        game.stopGame(Game.Player.PLAYER2);
        try {
            App.getInstance().navigateScene("lobby", "Lobby");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Returns a Circle with low opacity that indicates of possible move.
     * Does doMove(x, y) when clicked
     * @param   x   x-coordinate of piece
     * @param   y   y-coordinate of piece
     * @return      styled Circle that does doMove(x, y) on click
     */
    private Circle possibleMovePiece(int x, int y){
        Circle possibleMovePiece = new Circle();
        possibleMovePiece.setRadius(85);
        possibleMovePiece.setStrokeWidth(15);
        possibleMovePiece.setOpacity(0.1);
        possibleMovePiece.setStroke(Color.web("#E98074"));
        possibleMovePiece.setFill(Color.TRANSPARENT);
        possibleMovePiece.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            player1.doMove(x, y);
        });
        return possibleMovePiece;
    }

    /*
     * Returns the classic Boter Kaas en Eieren cross.
     */
    public Group createCross(){
        Line line1 = new Line();
        line1.setStartX(25);
        line1.setStartY(25);
        line1.setEndX(175);
        line1.setEndY(175);
        line1.setStrokeWidth(15);
        line1.setStroke(Color.web("#8E8D8A"));
        Line line2 = new Line();
        line2.setStartX(175);
        line2.setStartY(25);
        line2.setEndX(25);
        line2.setEndY(175);
        line2.setStrokeWidth(15);
        line2.setStroke(Color.web("#8E8D8A"));
        Group group = new Group(line1, line2);
        return group;
    }

    /*
     * Returns the classic Boter Kaas en Eieren circle.
     */
    public Circle createCircle(){
        Circle circle = new Circle();
        circle.setRadius(85);
        circle.setStrokeWidth(15);
        circle.setStroke(Color.web("#E98074"));
        circle.setFill(Color.TRANSPARENT);
        return circle;
    }
}
