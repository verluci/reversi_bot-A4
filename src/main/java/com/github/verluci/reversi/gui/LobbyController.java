package com.github.verluci.reversi.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class LobbyController extends AnchorPane {
    @FXML private ListView currentPlayers;


    public void initialize() {
        ObservableList<String> names = FXCollections.observableArrayList(
                "Julia", "Ian", "Sue", "Matthew", "Hannah", "Stephan", "Denise");
        currentPlayers.setItems(names);
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
    public void tickVsAI(ActionEvent event){

    }
}
