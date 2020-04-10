package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.networking.types.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.github.verluci.reversi.game.TicTacToeGame;

import java.io.IOException;

public class LobbyController extends AnchorPane {
    @FXML private ListView currentPlayers;
    @FXML private Text welkomSpeler;

    private App application;

    private static final Duration PROBE_FREQUENCY = Duration.seconds(1);

    private Timeline timeline;

    public void setApp(App app){
        this.application = app;
    }

    public void initialize() {
        welkomSpeler.setText("Welkom, " + application.getInstance().localPlayer.getName());
        updateCurrentPlayerList();
    }

    private void updateCurrentPlayerList(){
        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        actionEvent -> {
                            Player[] players = application.getInstance().gameClient.getPlayerList();
                            ObservableList<String> playersAr = FXCollections.observableArrayList();
                            for (int i = 0; i < players.length; i++) {
                                String playerName = players[i].getName();
                                if (!playerName.equals(application.getInstance().localPlayer.getName())) {
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
    public void othelloVsAI(ActionEvent event){

    }

    @FXML
    public void othelloVsPlayer(ActionEvent event){

    }

    @FXML
    public void tickVsPlayer(ActionEvent event){

    }

    @FXML
    public void tickVsAI(ActionEvent event) throws IOException {
        application.getInstance().navigateScene("boterkaaseneieren");
    }
}
