package com.github.lutzenh.gpgpu;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GPUSelectionBox extends Stage {

    private TableView<GraphicsDevice> graphicsDeviceTable;
    private TextArea textArea;

    private GraphicsDevice selectedGraphicsDevice;

    public GPUSelectionBox() {
        TableColumn<GraphicsDevice, DeviceType> typeColumn = new TableColumn<>("Type");
        typeColumn.setMinWidth(50);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<GraphicsDevice, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<GraphicsDevice, String> vendorColumn = new TableColumn<>("Vendor");
        vendorColumn.setMinWidth(40);
        vendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendor"));

        TableColumn<GraphicsDevice, String> driverVersionColumn = new TableColumn<>("Driver Version");
        driverVersionColumn.setMinWidth(50);
        driverVersionColumn.setCellValueFactory(new PropertyValueFactory<>("driverVersion"));

        TableColumn<GraphicsDevice, String> platformNameColumn = new TableColumn<>("Platform");
        platformNameColumn.setMinWidth(50);
        platformNameColumn.setCellValueFactory(new PropertyValueFactory<>("platformName"));

        TableColumn<GraphicsDevice, String> platformVersionColumn = new TableColumn<>("Platform Version");
        platformVersionColumn.setMinWidth(50);
        platformVersionColumn.setCellValueFactory(new PropertyValueFactory<>("platformVersion"));

        textArea = new TextArea();
        textArea.setWrapText(false);
        textArea.setEditable(false);

        Button selectButton = new Button("Select");
        selectButton.setDisable(true);
        selectButton.setDefaultButton(true);

        Button cancelButton = new Button("Cancel");

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(cancelButton, selectButton);

        graphicsDeviceTable = new TableView<>();
        graphicsDeviceTable.setItems(JOCLSample.getGraphicsDevices());
        graphicsDeviceTable.setPlaceholder(new Label("No Compatible GPU Drivers Found,\nMake sure you have OpenCL installed."));
        graphicsDeviceTable.getColumns().addAll(typeColumn, nameColumn, vendorColumn, driverVersionColumn, platformNameColumn, platformVersionColumn);

        graphicsDeviceTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
                        //Check whether item is selected and set value of selected item to Label
                        if(graphicsDeviceTable.getSelectionModel().getSelectedItem() != null)
                        {
                            GraphicsDevice graphicsDevice = graphicsDeviceTable.getSelectionModel().getSelectedItem();
                            textArea.setText(JOCLSample.getDeviceSpecifications(graphicsDevice.getId()));
                            selectButton.setDisable(false);
                        } else {
                            selectButton.setDisable(true);
                            textArea.setText("");
                        }
                    }
                });

        selectButton.setOnAction(e -> {
            selectedGraphicsDevice = graphicsDeviceTable.getSelectionModel().getSelectedItem();
            this.close();
        });

        cancelButton.setOnAction(e -> {
            this.close();
        });

        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(sp, Priority.ALWAYS);
        final StackPane sp1 = new StackPane(graphicsDeviceTable);
        final StackPane sp2 = new StackPane(textArea);
        sp.getItems().addAll(sp1, sp2);
        sp.setDividerPositions(0.6f, 0.3f);

        VBox vbox = new VBox(sp, buttonBar);
        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(4))));
        var scene = new Scene(vbox, 500, 400);
        this.setScene(scene);
        this.setTitle("Choose your OpenCL Device");
    }

    public GraphicsDevice selectGraphicsDevice() {
        this.showAndWait();
        return selectedGraphicsDevice;
    }
}
