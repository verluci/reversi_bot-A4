package com.github.verluci.reversi;

import com.github.verluci.reversi.game.Game;
import com.github.verluci.reversi.game.SessionInitializer;
import com.github.verluci.reversi.game.agents.Agent;
import com.github.verluci.reversi.game.agents.LocalPlayerAgent;
import com.github.verluci.reversi.game.agents.NetworkAgent;
import com.github.verluci.reversi.gpgpu.GPUSelectionBox;
import com.github.verluci.reversi.gpgpu.GraphicsDevice;
import com.github.verluci.reversi.gpgpu.JOCLSample;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.TelnetGameClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.github.verluci.reversi.networking.clients.GameClient;

import java.io.IOException;


/**
 * JavaFX App
 */
public class App extends Application {

    GraphicsDevice selectedGraphicsDevice;

    private Stage primaryStage;

    public GameClient gameClient;
    public com.github.verluci.reversi.networking.types.Player localPlayer;
    public Agent player1;

    private static App instance;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;
        GPUSelectionBox gpuSelectionBox = new GPUSelectionBox();

        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        var label = new Label();

        Button gpuCalculationTest = new Button("Test OpenCL Device");
        gpuCalculationTest.setDisable(true);
        gpuCalculationTest.setOnAction(e -> {
            var passedSimpleGPUTest = JOCLSample.performSimpleGPUCalculationTest(
                    selectedGraphicsDevice.getId(),
                    selectedGraphicsDevice.getPlatform_id()
            );
        });

        Button selectGPUButton = new Button("Choose Compute Device");
        selectGPUButton.setOnAction(e -> {
            selectedGraphicsDevice = gpuSelectionBox.selectGraphicsDevice();
            var selectedGpuString = selectedGraphicsDevice != null ? selectedGraphicsDevice.getName() : "None";
            label.setText("Hello, JavaFX " + javafxVersion
                    + ",\nrunning on Java " + javaVersion
                    + ".\nOpenCL Device: " + selectedGpuString);
            gpuCalculationTest.setDisable(selectedGraphicsDevice == null);
        });
        var infoScene = new Scene(new VBox(label, selectGPUButton, gpuCalculationTest), 640, 480);

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void initializeConnection(String name) throws GameClientExceptions.ConnectionException, GameClientExceptions.LoginException {
        gameClient = new TelnetGameClient();
        gameClient.connect("localhost", 7789);
        this.localPlayer = new com.github.verluci.reversi.networking.types.Player(name);
        gameClient.login(name);
        player1 = new LocalPlayerAgent();
    }

    public void navigateScene(String scene) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource(scene + ".fxml"));
        Parent root = loader.load();
        Scene newScene = new Scene(root);
        primaryStage.setScene(newScene);
    }
}
