package com.estatex.adapter.web.storage;

import com.estatex.application.port.out.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path uploadDir;
    private final String baseUrl;

    public LocalFileStorageAdapter(
            @Value("${app.file-storage.upload-dir}") String uploadDir,
            @Value("${app.file-storage.base-url}") String baseUrl) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(String filename, InputStream data, String contentType) {
        var ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : "";
        var uniqueName = UUID.randomUUID() + ext;
        var target = uploadDir.resolve(uniqueName);
        try {
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + filename, e);
        }
        return baseUrl + "/" + uniqueName;
    }

    @Override
    public void delete(String url) {
        if (url == null) return;
        var filename = url.substring(url.lastIndexOf('/') + 1);
        var path = uploadDir.resolve(filename);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log but don't throw — deletion failure is non-critical
        }
    }
}
