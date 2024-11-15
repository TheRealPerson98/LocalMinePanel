package com.person98.localminepanel.core;

import java.io.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.nio.file.Path;
import java.nio.file.Files;

import com.person98.localminepanel.services.template.ServerTemplate;
import lombok.Getter;
import lombok.Setter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

@Getter
@Setter
public class Server implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String uuid;
    private String name;
    private String type;
    private String software;
    private String serverJar;
    private String ip = "0.0.0.0";
    private int port = 25565;
    private int memory = 1024;
    private String javaArgs = "-Dterminal.jline=false -Dterminal.ansi=true";
    private transient ServerProcess process;
    private transient ServerTemplate template;
    private String startupCommand;
    private transient ObjectProperty<ServerProcess> processProperty = new SimpleObjectProperty<>();

    public Server(String name, String type, String software) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.software = software;
        this.processProperty.set(null);
    }
    
    public String getServerPath() {
        return System.getenv("APPDATA") + "\\.minepanellocal\\servers\\" + uuid;
    }

    public void setTemplate(ServerTemplate template) {
        this.template = template;
    }

    public String getStartupCommand() {
        if (startupCommand != null && !startupCommand.isEmpty()) {
            return startupCommand
                .replace("{memory}", String.valueOf(memory))
                .replace("{java_args}", javaArgs)
                .replace("{server_jar}", serverJar);
        }
        
        // Use template or fallback
        if (template != null) {
            return template.getStartup().getCommand()
                .replace("{memory}", String.valueOf(memory))
                .replace("{java_args}", javaArgs)
                .replace("{server_jar}", serverJar);
        }
        
        // Fallback
        return String.format("java -Xms128M -Xmx%dM %s -jar %s", 
            memory, javaArgs, serverJar);
    }

    public void setStartupCommand(String command) {
        this.startupCommand = command;
    }

    public void start() throws IOException {
        if (processProperty.get() == null) {
            processProperty.set(new ServerProcess(this));
        }
        processProperty.get().start();
    }

    public void stop() throws IOException {
        if (processProperty.get() != null) {
            processProperty.get().stop();
            processProperty.set(null);
        }
    }

    public boolean isRunning() {
        return processProperty != null && 
               processProperty.get() != null && 
               processProperty.get().isRunning();
    }

    public ServerProcess getProcess() {
        return processProperty.get();
    }

    public ObjectProperty<ServerProcess> processProperty() {
        return processProperty;
    }

    @Override
    public String toString() {
        return name;
    }

    public void saveServerProperties(Map<String, String> properties) throws IOException {
        Path propsPath = Path.of(getServerPath(), "server.properties");
        Properties props = new Properties();
        props.putAll(properties);
        
        try (OutputStream out = Files.newOutputStream(propsPath)) {
            props.store(out, "Minecraft server properties");
        }
    }

    public Map<String, String> loadServerProperties() throws IOException {
        Path propsPath = Path.of(getServerPath(), "server.properties");
        Properties props = new Properties();
        
        if (Files.exists(propsPath)) {
            try (InputStream in = Files.newInputStream(propsPath)) {
                props.load(in);
            }
        }
        
        return new HashMap<>((Map) props);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reinitialize transient fields
        processProperty = new SimpleObjectProperty<>();
    }
} 