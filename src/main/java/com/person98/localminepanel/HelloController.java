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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.HashMap;
import javafx.stage.FileChooser;

public class HelloController {
    @FXML private ListView<Server> serverListView;
    @FXML private VBox serverDetailsPane;
    @FXML private Label addressLabel;
    @FXML private Label uptimeLabel;
    @FXML private Label cpuLabel;
    @FXML private Label memoryLabel;
    @FXML private TextArea consoleOutput;
    @FXML private TextField commandInput;
    @FXML private TreeView<String> fileTreeView;
    @FXML private VBox serverPropertiesBox;
    @FXML private TextField memoryField;
    @FXML private TextField javaArgsField;
    @FXML private TextField portField;
    @FXML private TextField ipField;
    
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
            
            // Update basic info
            addressLabel.setText(server.getIp() + ":" + server.getPort());
            
            // Update settings fields
            memoryField.setText(String.valueOf(server.getMemory()));
            javaArgsField.setText(server.getJavaArgs());
            portField.setText(String.valueOf(server.getPort()));
            ipField.setText(server.getIp());
            
            // Update server properties
            updateServerProperties();
            
            // Update file tree
            updateFileTree();
            
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
    
    @FXML
    private void handleSaveSettings() {
        if (selectedServer != null) {
            try {
                selectedServer.setMemory(Integer.parseInt(memoryField.getText()));
                selectedServer.setJavaArgs(javaArgsField.getText());
                selectedServer.setPort(Integer.parseInt(portField.getText()));
                selectedServer.setIp(ipField.getText());
                
                // Save server properties
                saveServerProperties();
                
                // Save to disk
                ServerManager.getInstance().saveServers();
                
                showInfo("Settings saved successfully");
            } catch (Exception e) {
                showError("Failed to save settings", e);
            }
        }
    }
    
    private void updateFileTree() {
        if (selectedServer != null) {
            try {
                Path serverDir = Path.of(selectedServer.getServerPath());
                TreeItem<String> root = new TreeItem<>(serverDir.getFileName().toString());
                populateFileTree(root, serverDir);
                fileTreeView.setRoot(root);
                root.setExpanded(true);
            } catch (IOException e) {
                showError("Failed to load server files", e);
            }
        }
    }
    
    private void populateFileTree(TreeItem<String> item, Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                TreeItem<String> node = new TreeItem<>(entry.getFileName().toString());
                if (Files.isDirectory(entry)) {
                    populateFileTree(node, entry);
                }
                item.getChildren().add(node);
            }
        }
    }
    
    private void updateServerProperties() {
        if (selectedServer != null) {
            try {
                // Clear existing properties
                serverPropertiesBox.getChildren().clear();
                
                // Load properties from file
                Map<String, String> properties = selectedServer.loadServerProperties();
                
                // Create input fields for each property
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    Label label = new Label(entry.getKey());
                    TextField field = new TextField(entry.getValue());
                    
                    // Add change listener to save properties when modified
                    field.textProperty().addListener((obs, oldVal, newVal) -> {
                        try {
                            properties.put(entry.getKey(), newVal);
                            selectedServer.saveServerProperties(properties);
                        } catch (IOException e) {
                            showError("Failed to save properties", e);
                        }
                    });
                    
                    serverPropertiesBox.getChildren().addAll(label, field);
                }
            } catch (IOException e) {
                showError("Failed to load server properties", e);
            }
        }
    }
    
    private void saveServerProperties() {
        if (selectedServer != null) {
            try {
                // Collect all properties from the UI
                Map<String, String> properties = new HashMap<>();
                for (int i = 0; i < serverPropertiesBox.getChildren().size(); i += 2) {
                    Label label = (Label) serverPropertiesBox.getChildren().get(i);
                    TextField field = (TextField) serverPropertiesBox.getChildren().get(i + 1);
                    properties.put(label.getText(), field.getText());
                }
                
                // Save to file
                selectedServer.saveServerProperties(properties);
            } catch (IOException e) {
                showError("Failed to save server properties", e);
            }
        }
    }
    
    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    @FXML
    private void handleUploadFile() {
        if (selectedServer != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Upload File");
            File file = fileChooser.showOpenDialog(serverDetailsPane.getScene().getWindow());
            
            if (file != null) {
                try {
                    ServerFileManager.uploadFile(selectedServer, file);
                    updateFileTree();
                    showInfo("File uploaded successfully");
                } catch (IOException e) {
                    showError("Failed to upload file", e);
                }
            }
        }
    }
    
    @FXML
    private void handleDownloadFile() {
        if (selectedServer != null && fileTreeView.getSelectionModel().getSelectedItem() != null) {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            String fileName = selectedItem.getValue();
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog(serverDetailsPane.getScene().getWindow());
            
            if (file != null) {
                try {
                    ServerFileManager.downloadFile(selectedServer, fileName, file);
                    showInfo("File downloaded successfully");
                } catch (IOException e) {
                    showError("Failed to download file", e);
                }
            }
        }
    }
    
    @FXML
    private void handleDeleteFile() {
        if (selectedServer != null && fileTreeView.getSelectionModel().getSelectedItem() != null) {
            TreeItem<String> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
            String fileName = selectedItem.getValue();
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete File");
            confirm.setHeaderText("Delete " + fileName + "?");
            confirm.setContentText("Are you sure you want to delete this file? This cannot be undone.");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    ServerFileManager.deleteFile(selectedServer, fileName);
                    updateFileTree();
                    showInfo("File deleted successfully");
                } catch (IOException e) {
                    showError("Failed to delete file", e);
                }
            }
        }
    }
    
    @FXML
    private void handleArchiveFiles() {
        if (selectedServer != null) {
            TextInputDialog dialog = new TextInputDialog("backup");
            dialog.setTitle("Create Archive");
            dialog.setHeaderText("Enter archive name");
            dialog.setContentText("Archive name (without extension):");
            
            dialog.showAndWait().ifPresent(name -> {
                try {
                    ServerFileManager.createArchive(selectedServer, name);
                    showInfo("Archive created successfully");
                } catch (IOException e) {
                    showError("Failed to create archive", e);
                }
            });
        }
    }
}