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
import java.util.Properties;

public class LobbyController extends AnchorPane {
    @FXML Button OthelloMoeilijk;
    @FXML private ListView currentPlayers;
    @FXML private Text welkomSpeler;

    private static final Duration PROBE_FREQUENCY = Duration.seconds(1);

    private Timeline timeline;

    public void initialize() {
        welkomSpeler.setText("Welkom, " + App.getInstance().getLocalPlayer().getName());
        if(App.getInstance().getSelectedGraphicsDevice() == null){
            OthelloMoeilijk.setOnAction(Event::consume);
            OthelloMoeilijk.setOpacity(0.5);
            OthelloMoeilijk.setTooltip(new Tooltip("Kies een GPU om Othello op moeilijk te kunnen spelen (herstart hiervoor de app en ga naar Instellingen)"));
        }
    }

    private void updateCurrentPlayerList(){
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

    @FXML
    public void othelloVsPlayer(ActionEvent event) {
        navigateOthello( true, Difficulty.MAKKELIJK);
    }

    @FXML
    public void othelloVsMakkelijk(ActionEvent event) {
         navigateOthello(false, Difficulty.MAKKELIJK);
    }

    @FXML
    public void othelloVsNormaal(ActionEvent event) {
        navigateOthello(false, Difficulty.NORMAAL);
    }

    @FXML
    public void othelloVsMoeilijk(ActionEvent event) {
        navigateOthello(false, Difficulty.MOEILIJK);
    }

    @FXML
    public void tickVsPlayer(ActionEvent event) {
        navigateTickTackToe( true, Difficulty.MAKKELIJK);
    }

    @FXML
    public void tickVsMakkelijk(ActionEvent event) {
        navigateTickTackToe(false, Difficulty.MAKKELIJK);
    }

    @FXML
    public void tickVsNormaal(ActionEvent event) {
        navigateTickTackToe(false, Difficulty.NORMAAL);
    }

    public void navigateOthello(boolean online, Difficulty difficulty){
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("othello.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OthelloController controller = loader.getController();


        if(online) {
            controller.setupMultiplayerGame();
        } else {
            controller.setupAIGame(difficulty);
        }
        Scene newScene = new Scene(root);
        App.getInstance().getPrimaryStage().setScene(newScene);
    }

    public void navigateTickTackToe(boolean online, Difficulty difficulty){
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("boterkaaseneieren.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BoterKaasEnEierenController controller = loader.getController();


        if(online) {
            controller.setupMultiplayerGame();
        } else {
            controller.setupAIGame(difficulty);
        }
        Scene newScene = new Scene(root);
        App.getInstance().getPrimaryStage().setScene(newScene);
    }
}
