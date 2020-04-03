package com.github.verluci.reversi;

import com.github.verluci.reversi.gpgpu.GPUSelectionBox;
import com.github.verluci.reversi.gpgpu.GraphicsDevice;
import com.github.verluci.reversi.gpgpu.JOCLSample;
import com.github.verluci.reversi.gui.LoginController;
import com.github.verluci.reversi.gui.ScreenController;
import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * JavaFX App
 */
public class App extends Application {

    GraphicsDevice selectedGraphicsDevice;
    private Stage primaryStage;

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
}
