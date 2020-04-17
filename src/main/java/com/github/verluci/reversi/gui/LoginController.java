package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.networking.GameClientExceptions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Properties;

/*
 * Scene for logging in
 */
public class LoginController extends AnchorPane {
    Properties properties;

    @FXML private TextField userName;
    @FXML private Label loginError;

    /*
     *  Method to initialize this scene UI.
     *  Should not be called manually, done by JavaFX
     */
    public void initialize() {
        properties = App.getInstance().getProperties();
        loginError.setText("");

        if (properties.getProperty("gpuName").equals("")) {
            loginError.setText("Er is geen GPU gevonden. Selecteer er een in Instellingen");
        }
    }

    /*
     *  Used by FXML Button to login player
     *  @param  event  event from Button
     */
    public void loginUser(ActionEvent event) {
        try {
            if (userName.getText() == null || userName.getText().trim().isEmpty()) {
                loginError.setText("Vul een geldige gebruikersnaam in");
            } else {
                App.getInstance().initializeConnection(userName.getText());
                App.getInstance().navigateScene("lobby");
            }
        } catch (GameClientExceptions.LoginException e) {
            loginError.setText("Deze naam is al in gebruik");
        } catch (GameClientExceptions.ConnectionException e) {
            loginError.setText("Er is een probleem met deze server. Kies een andere of probeer het later nog eens");
        } catch (IOException e) {
            loginError.setText("Er ging iets fout. Probeer het later nog eens.");
        }
    }

    /*
     *  Used by FXML Button to navigate to settings
     *  @param  event  event from Button
     */
    public void goToSettings(ActionEvent actionEvent) throws IOException {
        App.getInstance().navigateScene("settings");
    }
}
