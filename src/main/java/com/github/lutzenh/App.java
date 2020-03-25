package com.github.lutzenh;

import com.github.lutzenh.gpgpu.GPUSelectionBox;
import com.github.lutzenh.gpgpu.GraphicsDevice;
import com.github.lutzenh.gpgpu.JOCLSample;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    GraphicsDevice selectedGraphicsDevice;

    @Override
    public void start(Stage stage) {
        GPUSelectionBox gpuSelectionBox = new GPUSelectionBox();

        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        var label = new Label();

        Button gpuCalculationTest = new Button("Test OpenCL Device");
        gpuCalculationTest.setDisable(true);
        gpuCalculationTest.setOnAction(e -> {
            JOCLSample.performSimpleGPUCalculationTest(selectedGraphicsDevice.getId(), selectedGraphicsDevice.getPlatform_id());
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

        var scene = new Scene(new VBox(label, selectGPUButton, gpuCalculationTest), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
