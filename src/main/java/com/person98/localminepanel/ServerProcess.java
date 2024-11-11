package com.person98.localminepanel;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;

@Getter
public class ServerProcess {
    private final Server server;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean isRunning = false;
    private long startTime;
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private long pid;

    public ServerProcess(Server server) {
        this.server = server;
    }
    
    public void start() throws IOException {
        if (isRunning) return;
        
        ProcessBuilder builder = new ProcessBuilder();
        String startCommand = server.getStartupCommand();
        System.out.println("Starting server with command: " + startCommand);
        
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            builder.command("cmd", "/c", startCommand);
        } else {
            builder.command("sh", "-c", startCommand);
        }
        
        builder.directory(new File(server.getServerPath()));
        builder.redirectErrorStream(true);
        
        process = builder.start();
        pid = process.pid();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        
        isRunning = true;
        startTime = System.currentTimeMillis();
    }
    
    public void stop() throws IOException {
        if (!isRunning) return;
        
        sendCommand("stop");
        try {
            process.waitFor(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        
        isRunning = false;
    }
    
    public void sendCommand(String command) throws IOException {
        if (!isRunning) return;
        writer.write(command + "\n");
        writer.flush();
    }
    
    public String readLine() throws IOException {
        return reader.readLine();
    }
    
    public boolean isRunning() {
        return isRunning && process.isAlive();
    }
    
    public long getUptime() {
        return isRunning ? System.currentTimeMillis() - startTime : 0;
    }
    
    public double getCpuUsage() {
        if (!isRunning) return 0.0;
        try {
            return osBean.getProcessCpuLoad() * 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public long getMemoryUsage() {
        if (!isRunning) return 0L;
        try {
            long bytes = osBean.getProcessCpuTime();
            return bytes / (1024 * 1024); // Convert bytes to MB
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
}