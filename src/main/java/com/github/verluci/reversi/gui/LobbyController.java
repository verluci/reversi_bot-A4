package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.networking.types.Difficulty;
import com.github.verluci.reversi.networking.types.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;

/*
 * The scene for the game lobby
 */
public class LobbyController extends AnchorPane {
    private static final Duration PROBE_FREQUENCY = Duration.seconds(1);
    @FXML
    Button OthelloMoeilijk;
    @FXML
    private ListView currentPlayers;
    @FXML
    private Text welkomSpeler;
    private Timeline timeline;

    /*
     *  Method to initialize this scene UI.
     *  Should not be called manually, done by JavaFX
     */
    public void initialize() {
        welkomSpeler.setText("Welkom, " + App.getInstance().getLocalPlayer().getName());
        updateCurrentPlayerList();
        if (App.getInstance().getSelectedGraphicsDevice() == null) {
            OthelloMoeilijk.setOnAction(Event::consume);
            OthelloMoeilijk.setOpacity(0.5);
            OthelloMoeilijk.setTooltip(new Tooltip("Kies een GPU om Othello op moeilijk te kunnen spelen (herstart hiervoor de app en ga naar Instellingen)"));
        }
    }

    /*
     *  Method to update current player list. Updates every second.
     */
    private void updateCurrentPlayerList() {
        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        actionEvent -> {
                            Player[] players = App.getInstance().getGameClient().getPlayerList();
                            ObservableList<String> playersAr = FXCollections.observableArrayList();
                            for (int i = 0; i < players.length; i++) {
                                String playerName = players[i].getName();
                                if (!playerName.equals(App.getInstance().getLocalPlayer().getName())) {
                                    playersAr.add(playerName);
                                }
                            }
                            currentPlayers.setItems(playersAr);
                        }
                ),
                new KeyFrame(
                        PROBE_FREQUENCY
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /*
     *  Used by FXML Button to navigate to Othello multiplayer
     *  @param  event  event from Button
     */
    @FXML
    public void othelloVsPlayer(ActionEvent event) {
        navigateOthello(true, Difficulty.MAKKELIJK);
    }

    /*
     *  Used by FXML Button to navigate to Othello on easy difficulty
     *  @param  event  event from Button
     */
    @FXML
    public void othelloVsMakkelijk(ActionEvent event) {
        navigateOthello(false, Difficulty.MAKKELIJK);
    }

    /*
     *  Used by FXML Button to navigate to Othello on medium difficulty
     *  @param  event  event from Button
     */
    @FXML
    public void othelloVsNormaal(ActionEvent event) {
        navigateOthello(false, Difficulty.NORMAAL);
    }

    /*
     *  Used by FXML Button to navigate to Othello on hard difficulty
     *  @param  event  event from Button
     */
    @FXML
    public void othelloVsMoeilijk(ActionEvent event) {
        navigateOthello(false, Difficulty.MOEILIJK);
    }

    /*
     *  Used by FXML Button to navigate to Tick Tack Toe multiplayer
     *  @param  event  event from Button
     */
    @FXML
    public void tickVsPlayer(ActionEvent event) {
        navigateTickTackToe(true, Difficulty.MAKKELIJK);
    }

    /*
     *  Used by FXML Button to navigate to Boter Kaas en Eieren on easy difficulty
     *  @param  event  event from Button
     */
    @FXML
    public void tickVsMakkelijk(ActionEvent event) {
        navigateTickTackToe(false, Difficulty.MAKKELIJK);
    }

    /*
     *  Used by FXML Button to navigate to Boter Kaas en Eieren on medium difficulty
     *  @param  event  event from Button
     */
    @FXML
    public void tickVsNormaal(ActionEvent event) {
        navigateTickTackToe(false, Difficulty.NORMAAL);
    }

    /*
     *  Used to navigate to Othello
     *  @param  online      to navigate to an online game or not
     *  @param  difficulty  requested game difficulty
     */
    public void navigateOthello(boolean online, Difficulty difficulty) {
        timeline.stop();
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("othello.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OthelloController controller = loader.getController();


        if (online) {
            controller.setupMultiplayerGame();
        } else {
            controller.setupAIGame(difficulty);
        }
        Scene newScene = new Scene(root);
        App.getInstance().getPrimaryStage().setScene(newScene);
        App.getInstance().getPrimaryStage().setTitle("Othello");
    }

    /*
     *  Used to navigate to Tick Tack Toe
     *  @param  online      to navigate to an online game or not
     *  @param  difficulty  requested game difficulty
     */
    public void navigateTickTackToe(boolean online, Difficulty difficulty) {
        timeline.stop();
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("boterkaaseneieren.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BoterKaasEnEierenController controller = loader.getController();

        if (online) {
            controller.setupMultiplayerGame();
        } else {
            controller.setupAIGame(difficulty);
        }
        Scene newScene = new Scene(root);
        App.getInstance().getPrimaryStage().setScene(newScene);
        App.getInstance().getPrimaryStage().setTitle("Boter Kaas en Eieren");
    }
}
