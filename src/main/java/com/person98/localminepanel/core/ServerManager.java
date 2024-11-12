package com.person98.localminepanel.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {
    private static ServerManager instance;
    private final ObservableList<Server> servers;
    private final Path savePath;

    private ServerManager() {
        servers = FXCollections.observableArrayList();
        
        // Get AppData/Roaming directory and create our app folder
        String appData = System.getenv("APPDATA");
        Path appDir = Path.of(appData, ".minepanellocal");
        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Set save file path
        savePath = appDir.resolve("servers.json");
        System.out.println("Server data location: " + savePath);
        
        loadServers();
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public ObservableList<Server> getServers() {
        return servers;
    }

    public void addServer(Server server) {
        servers.add(server);
        saveServers();
    }

    private void loadServers() {
        if (Files.exists(savePath)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(savePath.toFile()))) {
                List<Server> loadedServers = (List<Server>) ois.readObject();
                servers.addAll(loadedServers);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveServers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savePath.toFile()))) {
            oos.writeObject(new ArrayList<>(servers));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeServer(Server server) {
        servers.remove(server);
        saveServers();
    }
} 