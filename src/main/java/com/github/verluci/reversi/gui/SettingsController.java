package com.github.verluci.reversi.gui;

import com.github.verluci.reversi.App;
import com.github.verluci.reversi.gpgpu.GPUSelectionBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SettingsController extends AnchorPane {
    Properties properties;

    @FXML TextField ipAddress;
    @FXML TextField port;
    @FXML TextField threads;
    @FXML TextField turnTime;
    @FXML Text gpuName;

    public void initialize(){
        findProperties();
        System.out.println();
        port.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    port.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        threads.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    threads.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        turnTime.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    turnTime.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    private void findProperties(){
        properties = App.getInstance().getProperties();

        ipAddress.setText(properties.get("ipAddress").toString());
        port.setText(properties.get("port").toString());
        threads.setText(properties.get("threads").toString());
        turnTime.setText(properties.get("turnTime").toString());
        gpuName.setText(properties.get("gpuName").toString());
    }

    public void opslaan() throws IOException {
        Path configFileLocation = Paths.get(System.getProperty("user.home"), ".verluci-reversi", "config.properties");
        BufferedWriter out = Files.newBufferedWriter(configFileLocation);


        properties.setProperty("ipAddress", ipAddress.getText().trim());
        properties.setProperty("port", port.getText());
        properties.setProperty("threads", threads.getText());
        properties.setProperty("turnTime", turnTime.getText());
        properties.setProperty("gpuName", gpuName.getText());
        properties.store(out, null);

        App.getInstance().getSelectedGraphicsDevice().setEstimatePerformance(Integer.parseInt(properties.getProperty("threads")));

        App.getInstance().navigateScene("login");
    }

    public void chooseGPU(ActionEvent actionEvent) {
        GPUSelectionBox gpuSelectionBox = new GPUSelectionBox(Float.parseFloat(turnTime.getText()));
        var selectedGraphicsDevice = gpuSelectionBox.selectGraphicsDevice();
        var selectedGpuString = selectedGraphicsDevice != null ? selectedGraphicsDevice.getName() : "";

        App.getInstance().setSelectedGraphicsDevice(selectedGraphicsDevice);

        properties.setProperty("threads", String.valueOf(selectedGraphicsDevice.getEstimatePerformance()));
        threads.setText(String.valueOf(selectedGraphicsDevice.getEstimatePerformance()));
        gpuName.setText(selectedGpuString);
        properties.setProperty("gpuName", selectedGpuString);
    }
}
