package com.estatex.application.acceptance.fakes;

import com.estatex.application.port.out.FileStoragePort;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryFileStoragePort implements FileStoragePort {

    private final Map<String, byte[]> storage = new HashMap<>();

    @Override
    public String store(String filename, InputStream data, String contentType) {
        try {
            byte[] bytes = data.readAllBytes();
            String url = "http://localhost:8080/files/" + UUID.randomUUID() + "/" + filename;
            storage.put(url, bytes);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        storage.remove(fileUrl);
    }
}
