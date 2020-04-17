package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.game.*;
import com.github.verluci.reversi.game.agents.*;
import com.github.verluci.reversi.networking.clients.GameClient;
import com.github.verluci.reversi.networking.types.Difficulty;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/*
 * The scene for playing Othello
 */
public class OthelloController extends AnchorPane {
    LocalUIPlayerAgent player1;
    Agent player2;
    Thread sessionThread;
    SessionInitializer session;
    Game game;

    private com.github.verluci.reversi.networking.types.Player localPlayer;
    private GameClient gameClient;

    @FXML private GridPane othpane;
    @FXML private Text status;
    @FXML private Text wit;
    @FXML private Text zwart;
    @FXML private Button exitButton;

    /*
     *  Method to initialize this scene UI.
     *  Should not be called manually, done by JavaFX
     */
    public void initialize() {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image("/hout.jpg"),
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        othpane.setBackground(new Background(backgroundImage));
        exitButton.setVisible(false);
        wit.setText("Wit: 0");
        zwart.setText("Zwart: 0");

        localPlayer = App.getInstance().getLocalPlayer();
        player1 = new LocalUIPlayerAgent();
    }

    /*
     *  Method to update the gameboard.
     *  Sets all pieces to the right state
     */
    private void updateGameBoard() {
        othpane.getChildren().retainAll(othpane.getChildren().get(0));

        Tile[][] tiles = game.getBoard().getTiles();

        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j].getState() == TileState.POSSIBLE_MOVE) {
                    if (game.getCurrentPlayer().equals(player1.getPlayer())) {
                        Circle possibleMovePiece = createPossibleMovePiece(i, j);
                        othpane.add(possibleMovePiece, i, j);
                    }
                }
                else if (tiles[i][j].getState() == TileState.PLAYER1) {
                    ImageView whitePiece = createRegularPiece(true);
                    othpane.add(whitePiece, i, j);
                }
                else if (tiles[i][j].getState() == TileState.PLAYER2) {
                    ImageView blackPiece = createRegularPiece(false);
                    othpane.add(blackPiece, i, j);
                }
            }
        }

        wit.setText("Wit: " + game.getPlayerScore(Game.Player.PLAYER1));
        zwart.setText("Zwart: " + game.getPlayerScore(Game.Player.PLAYER2));
    }

    /*
     *  Method to setup a game VS AI. Sets up AI of requested difficulty and starts game.
     *  @param  difficulty  The difficulty that the player wants to play the game on
     */
    public void setupAIGame(Difficulty difficulty) {
        if (difficulty == Difficulty.MAKKELIJK) {
            player2 = new FirstMoveAIAgent();
        } else if (difficulty == Difficulty.NORMAAL) {
            player2 = new RandomMoveAIAgent();
        } else if (difficulty == Difficulty.MOEILIJK) {
            player2 = new MCTSAIAgent(App.getInstance().getSelectedGraphicsDevice());
        }
        session = new SessionInitializer(player1, player2, OthelloGame.class);
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
        gameClient = App.getInstance().getGameClient();
        player2 = new NetworkAgent(gameClient, localPlayer);
        com.github.verluci.reversi.networking.types.Player[] startingPlayer = {null};

        try {
            gameClient.subscribeToGame("Reversi");
        } catch (Exception e) {
            e.printStackTrace();
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

            if(listener.getStartingPlayer().equals(localPlayer))
                session = new SessionInitializer(
                        player1,
                        player2,
                        OthelloGame.class);
            else
                session = new SessionInitializer(
                        player2,
                        player1,
                        OthelloGame.class);

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
            status.setText("Je speelt Othello!");
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
            if (gameClient != null)
                gameClient.forfeit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        game.stopGame(Game.Player.PLAYER2);
        try {
            App.getInstance().navigateScene("lobby", "Lobby");
        } catch (Exception e) {
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
    private Circle createPossibleMovePiece(int x, int y){
        Circle circle = new Circle();
        circle.setRadius(35);
        circle.setStrokeWidth(10);
        circle.setFill(Color.WHITE);
        circle.setOpacity(0.3);

        circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            player1.doMove(x, y);
        });
        return circle;
    }

    /*
     * Returns a black or white piece for on the game board
     * @param   white   whether the required game piece is white or black
     * @return          ImageView with the required game piece
     */
    private ImageView createRegularPiece(boolean white){
        Image image;
        if(white) {
            image = new Image("/othello_wit.png");
        } else {
            image = new Image("/othello_zwart.png");
        }
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(70);
        imageView.setPreserveRatio(true);
        imageView.setCache(true);
        return imageView;
    }
}


