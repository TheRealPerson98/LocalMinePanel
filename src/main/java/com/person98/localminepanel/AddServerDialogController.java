package com.person98.localminepanel;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.util.Map;
import java.util.HashMap;

public class AddServerDialogController {
    @FXML private ComboBox<String> serverTypeComboBox;
    @FXML private ComboBox<String> serverSoftwareComboBox;
    @FXML private TextField serverNameField;
    
    private final Map<String, String[]> serverOptions = new HashMap<>();
    
    @FXML
    public void initialize() {
        setupServerOptions();
        setupListeners();
        serverTypeComboBox.getSelectionModel().selectFirst();
    }
    
    private void setupServerOptions() {
        serverOptions.put("Bedrock", new String[]{
            "LiteLoader-bedrock",
            "PowerNukkitX",
            "bedrock",
            "gomint",
            "nukkit",
            "pocketmine_mp"
        });
        
        serverOptions.put("Java", new String[]{
            "cuberite", "curseforge", "fabric", "feather", "folia",
            "forge/forge", "ftb", "glowstone", "krypton", "limbo",
            "magma", "modrinth", "mohist", "nanolimbo", "neoforge",
            "paper", "purpur", "quilt", "spigot", "spongeforge",
            "spongevanilla", "technic", "vanillacord"
        });
        
        serverOptions.put("Proxy", new String[]{
            "Java/Travertine",
            "Java/Velocity",
            "Java/VIAaaS",
            "Java/Waterfall",
            "Bedrock/Waterdog PE",
            "Cross Platform/GeyserMC",
            "Cross Platform/Waterdog"
        });
    }
    
    private void setupListeners() {
        serverTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                serverSoftwareComboBox.setItems(
                    FXCollections.observableArrayList(serverOptions.get(newVal))
                );
            }
        });
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    @FXML
    private void handleCreate() {
        // TODO: Implement server creation logic
        closeDialog();
    }
    
    private void closeDialog() {
        ((Stage) serverTypeComboBox.getScene().getWindow()).close();
    }
} 