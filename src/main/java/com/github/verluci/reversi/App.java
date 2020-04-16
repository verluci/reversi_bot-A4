package com.github.verluci.reversi;

import com.github.verluci.reversi.game.agents.Agent;
import com.github.verluci.reversi.game.agents.LocalPlayerAgent;
import com.github.verluci.reversi.game.agents.LocalUIPlayerAgent;
import com.github.verluci.reversi.gpgpu.GPUSelectionBox;
import com.github.verluci.reversi.gpgpu.GraphicsDevice;
import com.github.verluci.reversi.gpgpu.JOCLSample;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.TelnetGameClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.github.verluci.reversi.networking.clients.GameClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.lang.Integer.parseInt;


/**
 * JavaFX App
 */
public class App extends Application {

    GraphicsDevice selectedGraphicsDevice;

    public Stage primaryStage;

    public GameClient gameClient;
    public com.github.verluci.reversi.networking.types.Player localPlayer;

    private static App instance;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public Properties properties;

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupConfig();
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
        gameClient.connect(properties.getProperty("ipAddress"), parseInt(properties.getProperty("port")));
        this.localPlayer = new com.github.verluci.reversi.networking.types.Player(name);
        gameClient.login(name);
    }

    public void navigateScene(String scene) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource(scene + ".fxml"));
        Parent root = loader.load();
        Scene newScene = new Scene(root);
        primaryStage.setScene(newScene);
    }

    private void setupConfig() {
        Path configFileLocation = Paths.get(System.getProperty("user.home"), ".verluci-reversi", "config.properties");
        if(!Files.exists(configFileLocation.getParent())){
            try {
                Files.createDirectory(configFileLocation.getParent());
            } catch (Exception e){
                System.out.println("Something went wrong");
            }
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/config.properties")));
                BufferedWriter out = Files.newBufferedWriter(configFileLocation);
            ){

                in.lines().forEach(line -> {
                    try {
                        out.append(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.out.println("Something went wrong");
            }
        }
        properties = new Properties();
        try (BufferedReader in = Files.newBufferedReader(configFileLocation)) {
            properties.load(in);
        } catch (IOException exc) {
            System.out.println("Something went wrong");
        }
    }
}
