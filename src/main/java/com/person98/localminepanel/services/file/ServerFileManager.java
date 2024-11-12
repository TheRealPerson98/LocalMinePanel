package com.person98.localminepanel.services.file;

import com.person98.localminepanel.core.Server;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.util.List;

public class ServerFileManager {
    public static void uploadFile(Server server, File sourceFile, String targetFolder) throws IOException {
        Path targetPath = Path.of(server.getServerPath(), targetFolder, sourceFile.getName());
        Files.createDirectories(targetPath.getParent());
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void uploadFile(Server server, File sourceFile) throws IOException {
        uploadFile(server, sourceFile, "");
    }

    public static void downloadFile(Server server, String fileName, File targetFile) throws IOException {
        Path sourcePath = Path.of(server.getServerPath(), fileName);
        Files.copy(sourcePath, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void deleteFile(Server server, String fileName) throws IOException {
        Path filePath = Path.of(server.getServerPath(), fileName);
        Files.delete(filePath);
    }

    public static void createArchive(Server server, String archiveName, List<String> files) throws IOException {
        Path serverDir = Path.of(server.getServerPath());
        Path archivePath = serverDir.getParent().resolve(archiveName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archivePath.toFile()))) {
            for (String fileName : files) {
                Path filePath = serverDir.resolve(fileName);
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    Files.copy(filePath, zos);
                    zos.closeEntry();
                }
            }
        }
    }

    public static void createArchive(Server server, String archiveName) throws IOException {
        Path serverDir = Path.of(server.getServerPath());
        Path archivePath = serverDir.getParent().resolve(archiveName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archivePath.toFile()))) {
            Files.walk(serverDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String zipEntry = serverDir.relativize(path).toString();
                            zos.putNextEntry(new ZipEntry(zipEntry));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
}