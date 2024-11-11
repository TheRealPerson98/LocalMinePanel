package com.person98.localminepanel;

import java.io.Serializable;
import java.util.UUID;
import java.io.IOException;
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


    public Server(String name, String type, String software) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.software = software;
    }
    
    public String getServerPath() {
        return System.getenv("APPDATA") + "\\.minepanellocal\\servers\\" + uuid;
    }
    public String getStartupCommand() {
        return String.format("java -Xms128M -Xmx%dM %s -jar %s", 
            memory, javaArgs, serverJar);
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
} 