package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.networking.GameClientExceptions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class LoginController extends AnchorPane {
    @FXML private TextField userName;
    @FXML private Label loginError;

    private App application;

    public void setApp(App app){
        this.application = app;
    }

    public void initialize() {
        loginError.setText("");
    }

    @FXML
    public void loginUser(ActionEvent event) {
        try{
            if (userName.getText() == null || userName.getText().trim().isEmpty()) {
                loginError.setText("Vul een geldige gebruikersnaam in");
            } else {
                application.getInstance().initializeConnection(userName.getText());
                application.getInstance().navigateScene("lobby");
            }
        } catch (GameClientExceptions.LoginException | GameClientExceptions.ConnectionException | IOException e) {
            loginError.setText("Deze naam is al in gebruik");
        }
    }
}
