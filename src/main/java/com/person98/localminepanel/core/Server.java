package com.person98.localminepanel.core;

import java.io.Serializable;
import java.util.UUID;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;

import com.person98.localminepanel.services.template.ServerTemplate;
import lombok.Getter;
import lombok.Setter;

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


    public Server(String name, String type, String software) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.software = software;
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
        if (process == null) {
            process = new ServerProcess(this);
        }
        process.start();
    }

    public void stop() throws IOException {
        if (process != null) {
            process.stop();
            process = null;
        }
    }

    public boolean isRunning() {
        return process != null && process.isRunning();
    }

    public ServerProcess getProcess() {
        return process;
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
} 