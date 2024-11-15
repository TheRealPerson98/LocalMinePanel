package com.person98.localminepanel.application.controllers;

import com.person98.localminepanel.core.Server;
import com.person98.localminepanel.services.file.ServerFileManager;
import com.person98.localminepanel.core.ServerManager;
import com.person98.localminepanel.core.ServerProcess;
import com.person98.localminepanel.application.views.ServerListCell;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
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

import javafx.stage.FileChooser;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.InputStream;
import java.io.OutputStream;

public class HelloController {
    @FXML private ListView<Server> serverListView;
    @FXML private VBox serverDetailsPane;
    @FXML private Label addressLabel;
    @FXML private Label uptimeLabel;
    @FXML private Label cpuLabel;
    @FXML private Label memoryLabel;
    @FXML private TextArea consoleOutput;
    @FXML private TextField commandInput;
    @FXML private TextField memoryField;
    @FXML private TextField javaArgsField;
    @FXML private TextField portField;
    @FXML private TextField ipField;
    @FXML private TextArea startupCommandField;
    @FXML private ListView<String> fileListView;
    @FXML private ListView<String> selectedFilesList;
    @FXML private TextField archiveNameField;
    @FXML private HBox breadcrumbNav;
    private String currentPath = "";

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

        // Setup drag and drop for file list
        fileListView.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        fileListView.setOnDragDropped(event -> {
            List<File> files = event.getDragboard().getFiles();
            if (files != null && selectedServer != null) {
                files.forEach(file -> {
                    try {
                        Path fullTargetPath = Path.of(selectedServer.getServerPath(), currentPath, file.getName());
                        Files.createDirectories(fullTargetPath.getParent());
                        Files.copy(file.toPath(), fullTargetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        showError("Failed to upload file", e);
                    }
                });
                updateFileList();
            }
            event.setDropCompleted(true);
            event.consume();
        });

        // Setup file list click handler
        fileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = fileListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (selected.equals("..")) {
                        // Go to parent directory
                        String parent = currentPath.substring(0, currentPath.lastIndexOf('/', currentPath.length() - 2) + 1);
                        navigateTo(parent);
                    } else if (selected.endsWith("/")) {
                        // Navigate into directory
                        navigateTo(currentPath + selected);
                    }
                }
            }
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/person98/localminepanel/add-server-dialog.fxml"));            Stage dialogStage = new Stage();
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
            saveServerProperties();
            
            // Update file tree
            updateFileList();
            
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
            
            startupCommandField.setText(server.getStartupCommand());
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
                selectedServer.setStartupCommand(startupCommandField.getText());
                
                // Save to disk
                ServerManager.getInstance().saveServers();
                
                showInfo("Settings saved successfully");
            } catch (Exception e) {
                showError("Failed to save settings", e);
            }
        }
    }
    
    private void updateFileList() {
        if (selectedServer != null) {
            try {
                Path currentDir = Path.of(selectedServer.getServerPath(), currentPath);
                fileListView.getItems().clear();
                
                // Add parent directory option if not in root
                if (!currentPath.isEmpty()) {
                    fileListView.getItems().add("..");
                }
                
                // List files and directories
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                    for (Path entry : stream) {
                        String fileName = entry.getFileName().toString();
                        if (Files.isDirectory(entry)) {
                            fileName += "/";
                        }
                        fileListView.getItems().add(fileName);
                    }
                }
                
                // Update breadcrumb navigation
                updateBreadcrumbs();
                
            } catch (IOException e) {
                showError("Failed to load directory contents", e);
            }
        }
    }
    
    private void updateBreadcrumbs() {
        breadcrumbNav.getChildren().clear();
        breadcrumbNav.getChildren().add(createBreadcrumbButton("/", ""));
        
        if (!currentPath.isEmpty()) {
            String[] parts = currentPath.split("/");
            StringBuilder path = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    path.append(part).append("/");
                    breadcrumbNav.getChildren().add(createBreadcrumbButton(part + "/", path.toString()));
                }
            }
        }
    }
    
    private Button createBreadcrumbButton(String label, String path) {
        Button btn = new Button(label);
        btn.getStyleClass().add("breadcrumb-button");
        btn.setOnAction(e -> navigateTo(path));
        return btn;
    }
    
    private void navigateTo(String path) {
        currentPath = path;
        updateFileList();
    }
    
    @FXML
    private void handleNavigateHome() {
        navigateTo("");
    }

    private void saveServerProperties() {
        if (selectedServer != null) {
            try {
                // Save properties to file
                Path propsPath = Path.of(selectedServer.getServerPath(), "server.properties");
                Properties props = new Properties();
                
                // Load existing properties first
                if (Files.exists(propsPath)) {
                    try (InputStream in = Files.newInputStream(propsPath)) {
                        props.load(in);
                    }
                }
                
                // Save back to file
                try (OutputStream out = Files.newOutputStream(propsPath)) {
                    props.store(out, "Minecraft server properties");
                }
                
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
    private void handleDownloadFile() {
        if (selectedServer != null) {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.endsWith("/") && !selected.equals("..")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                fileChooser.setInitialFileName(selected);
                File file = fileChooser.showSaveDialog(fileListView.getScene().getWindow());
                
                if (file != null) {
                    try {
                        Path sourcePath = Path.of(selectedServer.getServerPath(), currentPath, selected);
                        Files.copy(sourcePath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        showInfo("File downloaded successfully");
                    } catch (IOException e) {
                        showError("Failed to download file", e);
                    }
                }
            }
        }
    }

    @FXML
    private void handleDeleteFile() {
        if (selectedServer != null) {
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.equals("..")) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Delete");
                confirm.setHeaderText("Delete " + selected);
                confirm.setContentText("Are you sure you want to delete this " + 
                                     (selected.endsWith("/") ? "directory" : "file") + "?");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            Path path = Path.of(selectedServer.getServerPath(), currentPath, selected);
                            if (selected.endsWith("/")) {
                                Files.walk(path)
                                    .sorted((p1, p2) -> -p1.compareTo(p2))
                                    .forEach(p -> {
                                        try {
                                            Files.delete(p);
                                        } catch (IOException e) {
                                            showError("Failed to delete directory contents", e);
                                        }
                                    });
                            } else {
                                Files.delete(path);
                            }
                            updateFileList();
                            showInfo("Deleted successfully");
                        } catch (IOException e) {
                            showError("Failed to delete", e);
                        }
                    }
                });
            }
        }
    }

    @FXML
    private void handleAddToArchive(ActionEvent actionEvent) {
        String selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.equals("..")) {
            String fullPath = currentPath + selected;
            if (!selectedFilesList.getItems().contains(fullPath)) {
                selectedFilesList.getItems().add(fullPath);
            }
        }
    }

    @FXML
    private void handleCreateArchive(ActionEvent actionEvent) {
        if (selectedServer != null && !selectedFilesList.getItems().isEmpty()) {
            String archiveName = archiveNameField.getText();
            if (archiveName == null || archiveName.trim().isEmpty()) {
                showError("Invalid Archive Name", new Exception("Please enter an archive name"));
                return;
            }
            
            try {
                List<String> files = new ArrayList<>(selectedFilesList.getItems());
                ServerFileManager.createArchive(selectedServer, archiveName, files);
                selectedFilesList.getItems().clear();
                archiveNameField.clear();
                updateFileList();
                showInfo("Archive created successfully");
            } catch (IOException e) {
                showError("Failed to create archive", e);
            }
        } else {
            showError("No Files Selected", new Exception("Please select files to archive"));
        }
    }
}
