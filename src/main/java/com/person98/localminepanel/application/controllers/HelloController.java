package com.person98.localminepanel.application.controllers;

import com.person98.localminepanel.application.views.FileListCell;
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
import javafx.scene.text.Text;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;

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

    @FXML private TextArea fileEditor;
    private Path currentEditingFile;

    @FXML
    private CheckBox selectAllCheckbox;
    @FXML
    private HBox fileActionsBox;

    @FXML private VBox welcomeScreen;
    @FXML private TabPane serverTabPane;

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
            String selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                if (selected.equals("..")) {
                    navigateUp();
                } else if (Files.isDirectory(Paths.get(selectedServer.getServerPath(), currentPath, selected))) {
                    navigateToDirectory(selected);
                } else {
                    loadFileContent(selected);
                }
            }
        });

        // Add shutdown hook
        Platform.runLater(() -> {
            Stage stage = (Stage) serverListView.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                // Stop all running servers
                for (Server server : ServerManager.getInstance().getServers()) {
                    if (server.isRunning()) {
                        try {
                            server.stop();
                            // Wait briefly for server to stop
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });

        fileListView.setCellFactory(listView -> new FileListCell());

        welcomeScreen.setVisible(true);
        serverTabPane.setVisible(false);
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
                    Platform.runLater(() -> {
                        Text text = new Text(consoleLine + "\n");
                        
                        // Apply different styles based on the content
                        if (consoleLine.contains("INFO")) {
                            text.getStyleClass().add("info");
                        } else if (consoleLine.contains("WARN")) {
                            text.getStyleClass().add("warning");
                        } else if (consoleLine.contains("ERROR")) {
                            text.getStyleClass().add("error");
                        } else if (consoleLine.contains("Done")) {
                            text.getStyleClass().add("success");
                        }
                        
                        consoleOutput.appendText(consoleLine + "\n");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadFileContent(String filename) {
        if (selectedServer == null) return;
        
        try {
            Path filePath = Paths.get(selectedServer.getServerPath(), currentPath, filename);
            currentEditingFile = filePath;
            String content = Files.readString(filePath);
            fileEditor.setText(content);
            
            // Set editable based on file extension
            String extension = getFileExtension(filename).toLowerCase();
            boolean isEditable = isEditableFileType(extension);
            fileEditor.setEditable(isEditable);
            
        } catch (IOException e) {
            showError("Error Loading File", e);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private boolean isEditableFileType(String extension) {
        Set<String> editableExtensions = Set.of(
            "txt", "log", "properties", "json", "yml", "yaml", "xml",
            "css", "js", "java", "sh", "bat", "cmd", "ini", "cfg",
            "conf", "config", "md", "markdown", "html", "htm"
        );
        return editableExtensions.contains(extension.toLowerCase());
    }

    @FXML
    private void handleSaveFile() {
        if (currentEditingFile == null) return;
        
        try {
            Files.writeString(currentEditingFile, fileEditor.getText());
            showInfo("File saved successfully");
        } catch (IOException e) {
            showError("Error Saving File", e);
        }
    }

    @FXML
    private void handleReloadFile() {
        if (currentEditingFile == null) return;
        
        try {
            String content = Files.readString(currentEditingFile);
            fileEditor.setText(content);
            showInfo("File reloaded");
        } catch (IOException e) {
            showError("Error Reloading File", e);
        }
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
            welcomeScreen.setVisible(false);
            serverTabPane.setVisible(true);
            
            // Update basic info
            if (addressLabel != null) {
                addressLabel.setText(server.getIp() + ":" + server.getPort());
            }
            
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
            welcomeScreen.setVisible(true);
            serverTabPane.setVisible(false);
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

    private void navigateUp() {
        if (!currentPath.isEmpty()) {
            int lastSlash = currentPath.lastIndexOf('/', currentPath.length() - 2);
            currentPath = lastSlash >= 0 ? currentPath.substring(0, lastSlash + 1) : "";
            updateFileList();
        }
    }

    private void navigateToDirectory(String dirName) {
        currentPath += dirName + "/";
        updateFileList();
    }

    @FXML
    private void handleSelectAll() {
        boolean selectAll = selectAllCheckbox.isSelected();
        fileListView.getItems().forEach(item -> 
            fileListView.getProperties().put("selected_" + item, selectAll));
        fileListView.refresh();
        fileActionsBox.setVisible(selectAll);
    }

    @FXML
    private void handleDeleteFiles() {
        List<String> selectedFiles = getSelectedFiles();
        if (selectedFiles.isEmpty()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Selected Files");
        confirm.setContentText("Are you sure you want to delete " + selectedFiles.size() + " selected files?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                List<String> failedDeletes = new ArrayList<>();
                
                for (String fileName : selectedFiles) {
                    try {
                        Path filePath = Path.of(selectedServer.getServerPath(), currentPath, fileName);
                        if (Files.isDirectory(filePath)) {
                            // Delete directory and its contents
                            Files.walk(filePath)
                                .sorted(Comparator.reverseOrder())
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException e) {
                                        failedDeletes.add(fileName);
                                    }
                                });
                        } else {
                            Files.delete(filePath);
                        }
                    } catch (IOException e) {
                        failedDeletes.add(fileName);
                    }
                }

                if (!failedDeletes.isEmpty()) {
                    showError("Delete Operation", 
                        new Exception("Failed to delete: " + String.join(", ", failedDeletes)));
                } else {
                    showInfo("Files deleted successfully");
                }
                
                updateFileList();
                selectAllCheckbox.setSelected(false);
                fileActionsBox.setVisible(false);
            }
        });
    }

    @FXML
    private void handleArchiveFiles() {
        List<String> selectedFiles = getSelectedFiles();
        if (selectedFiles.isEmpty()) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Archive");
        dialog.setHeaderText("Enter archive name");
        dialog.setContentText("Archive name (without extension):");

        dialog.showAndWait().ifPresent(archiveName -> {
            if (!archiveName.trim().isEmpty()) {
                try {
                    // Create archive with selected files
                    ServerFileManager.createArchive(selectedServer, archiveName, 
                        selectedFiles.stream()
                            .map(file -> currentPath + file)
                            .collect(Collectors.toList())
                    );
                    
                    showInfo("Archive created successfully");
                    updateFileList();
                    selectAllCheckbox.setSelected(false);
                    fileActionsBox.setVisible(false);
                    
                } catch (IOException e) {
                    showError("Archive Creation Failed", e);
                }
            }
        });
    }

    @FXML
    private void handleMoveFiles() {
        List<String> selectedFiles = getSelectedFiles();
        if (selectedFiles.isEmpty()) return;

        // Create a dialog to select the destination directory
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Move Files");
        dialog.setHeaderText("Select destination directory");

        // Create a TreeView of server directories
        TreeView<String> dirTree = new TreeView<>();
        dirTree.setRoot(createDirectoryTree());
        dirTree.setPrefHeight(400);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(dirTree);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                TreeItem<String> selectedItem = dirTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    return getFullPath(selectedItem);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(targetPath -> {
            List<String> failedMoves = new ArrayList<>();
            
            for (String fileName : selectedFiles) {
                try {
                    Path sourcePath = Path.of(selectedServer.getServerPath(), currentPath, fileName);
                    Path targetDir = Path.of(selectedServer.getServerPath(), targetPath);
                    Path targetFilePath = targetDir.resolve(fileName);

                    // Create target directory if it doesn't exist
                    Files.createDirectories(targetDir);

                    // Move the file
                    Files.move(sourcePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    failedMoves.add(fileName);
                }
            }

            if (!failedMoves.isEmpty()) {
                showError("Move Operation", 
                    new Exception("Failed to move: " + String.join(", ", failedMoves)));
            } else {
                showInfo("Files moved successfully");
            }
            
            updateFileList();
            selectAllCheckbox.setSelected(false);
            fileActionsBox.setVisible(false);
        });
    }

    private TreeItem<String> createDirectoryTree() {
        TreeItem<String> root = new TreeItem<>("/");
        populateDirectoryTree(root, Path.of(selectedServer.getServerPath()), "");
        return root;
    }

    private void populateDirectoryTree(TreeItem<String> item, Path path, String relativePath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String dirName = entry.getFileName().toString();
                    TreeItem<String> dirItem = new TreeItem<>(dirName);
                    item.getChildren().add(dirItem);
                    
                    // Recursively populate subdirectories
                    populateDirectoryTree(dirItem, entry, 
                        relativePath.isEmpty() ? dirName : relativePath + "/" + dirName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFullPath(TreeItem<String> item) {
        StringBuilder path = new StringBuilder();
        TreeItem<String> current = item;
        
        while (current != null && !current.getValue().equals("/")) {
            path.insert(0, current.getValue()).insert(0, "/");
            current = current.getParent();
        }
        
        return path.toString();
    }

    private List<String> getSelectedFiles() {
        return fileListView.getItems().stream()
            .filter(item -> {
                Boolean selected = (Boolean) fileListView.getProperties().get("selected_" + item);
                return selected != null && selected;
            })
            .collect(Collectors.toList());
    }
}
