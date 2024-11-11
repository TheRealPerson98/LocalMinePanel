package com.person98.localminepanel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;
import java.io.IOException;

public class HelloController {
    @FXML private ListView<Server> serverListView;
    @FXML private VBox serverDetailsPane;
    @FXML private Label addressLabel;
    @FXML private Label uptimeLabel;
    @FXML private Label cpuLabel;
    @FXML private Label memoryLabel;
    @FXML private TextArea consoleOutput;
    @FXML private TextField commandInput;
    
    private Timeline updateTimeline;
    private Server selectedServer;
    
    @FXML
    public void initialize() {
        serverListView.setItems(ServerManager.getInstance().getServers());
        serverListView.setCellFactory(listView -> new ServerListCell());
        
        serverListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedServer = newSelection;
                    updateServerDetails(newSelection);
                }
            }
        );
        
        // Setup periodic updates
        updateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> updateMonitoring())
        );
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }
    
    private void updateMonitoring() {
        if (selectedServer != null && selectedServer.getProcess() != null) {
            ServerProcess process = selectedServer.getProcess();
            if (process.isRunning()) {
                Platform.runLater(() -> {
                    addressLabel.setText(selectedServer.getIp() + ":" + selectedServer.getPort());
                    uptimeLabel.setText(formatUptime(process.getUptime()));
                    cpuLabel.setText(String.format("%.1f%%", process.getCpuUsage()));
                    memoryLabel.setText(String.format("%dMB", process.getMemoryUsage()));
                });
            }
        }
    }
    
    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds % 60);
    }
    
    @FXML
    private void handleStartServer() {
        if (selectedServer != null && !selectedServer.isRunning()) {
            try {
                selectedServer.start();
                startConsoleMonitoring();
            } catch (IOException e) {
                showError("Failed to start server", e);
            }
        }
    }
    
    @FXML
    private void handleStopServer() {
        if (selectedServer != null && selectedServer.isRunning()) {
            try {
                selectedServer.stop();
            } catch (IOException e) {
                showError("Failed to stop server", e);
            }
        }
    }
    
    @FXML
    private void handleRestartServer() {
        if (selectedServer != null) {
            try {
                if (selectedServer.isRunning()) {
                    selectedServer.stop();
                }
                selectedServer.start();
                startConsoleMonitoring();
            } catch (IOException e) {
                showError("Failed to restart server", e);
            }
        }
    }
    
    @FXML
    private void handleSendCommand() {
        if (selectedServer != null && selectedServer.getProcess() != null) {
            String command = commandInput.getText();
            if (!command.isEmpty()) {
                try {
                    selectedServer.getProcess().sendCommand(command);
                    consoleOutput.appendText("\n> " + command);
                    commandInput.clear();
                } catch (IOException e) {
                    showError("Failed to send command", e);
                }
            }
        }
    }
    
    @FXML
    private void showAddServerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-server-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Add New Server");
            dialogStage.setScene(new Scene(loader.load()));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startConsoleMonitoring() {
        new Thread(() -> {
            try {
                String line;
                while ((line = selectedServer.getProcess().readLine()) != null) {
                    final String consoleLine = line;
                    Platform.runLater(() -> consoleOutput.appendText(consoleLine + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showError(String header, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        });
    }
    
    private void updateServerDetails(Server server) {
        if (server != null) {
            serverDetailsPane.setVisible(true);
            addressLabel.setText(server.getIp() + ":" + server.getPort());
            
            if (server.isRunning()) {
                ServerProcess process = server.getProcess();
                uptimeLabel.setText(formatUptime(process.getUptime()));
                cpuLabel.setText(String.format("%.1f%%", process.getCpuUsage()));
                memoryLabel.setText(String.format("%dMB / %dMB", process.getMemoryUsage(), server.getMemory()));
                consoleOutput.clear(); // Clear previous console output
                startConsoleMonitoring();
            } else {
                uptimeLabel.setText("Server Offline");
                cpuLabel.setText("0%");
                memoryLabel.setText("0MB / " + server.getMemory() + "MB");
                consoleOutput.clear();
            }
        } else {
            serverDetailsPane.setVisible(false);
        }
    }
}