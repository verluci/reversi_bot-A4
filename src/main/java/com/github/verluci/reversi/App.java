package com.github.verluci.reversi;

import com.github.verluci.reversi.gpgpu.GraphicsDevice;
import com.github.verluci.reversi.gpgpu.JOCLSample;
import com.github.verluci.reversi.networking.GameClientExceptions;
import com.github.verluci.reversi.networking.clients.TelnetGameClient;
import com.github.verluci.reversi.networking.types.Player;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    //region Singleton + Constructor

    private static App instance;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    //endregion

    //region Declarations

    private Stage primaryStage;
    private GraphicsDevice selectedGraphicsDevice;
    private GameClient gameClient;
    private com.github.verluci.reversi.networking.types.Player localPlayer;
    private Properties properties;

    //endregion

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupConfig();
        setupGPU();
        this.primaryStage = primaryStage;
        this.primaryStage.setResizable(false);

        this.primaryStage.setOnCloseRequest(windowEvent -> {
            if(gameClient != null && gameClient.getConnected()) {
                try {
                    gameClient.disconnect();
                } catch (GameClientExceptions.ConnectionException e) {
                    e.printStackTrace();
                }
            }
        });

        navigateScene("login", "Login");
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

    public void navigateScene(String scene, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource(scene + ".fxml"));
        Parent root = loader.load();
        root.setCache(true);
        Scene newScene = new Scene(root);
        primaryStage.setScene(newScene);


        primaryStage.setTitle(title);
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

    private void setupGPU(){
        if(!properties.getProperty("gpuName").equals("")){
            boolean foundDevice = false;
            var devices = JOCLSample.getGraphicsDevices();
            for (GraphicsDevice device : devices) {
                if (device.getName().equals(properties.getProperty("gpuName"))) {
                    selectedGraphicsDevice = device;
                    device.setEstimatePerformance(Integer.parseInt(properties.getProperty("threads")));
                    foundDevice = true;
                    break;
                }
            }
            if(!foundDevice){
                properties.setProperty("gpuName", "");
            }
        }
    }

    //region Getters and Setters

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public GraphicsDevice getSelectedGraphicsDevice() {
        return selectedGraphicsDevice;
    }

    public void setSelectedGraphicsDevice(GraphicsDevice selectedGraphicsDevice) {
        this.selectedGraphicsDevice = selectedGraphicsDevice;
    }

    public GameClient getGameClient() {
        return gameClient;
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public Properties getProperties() {
        return properties;
    }

    //endregion
}
