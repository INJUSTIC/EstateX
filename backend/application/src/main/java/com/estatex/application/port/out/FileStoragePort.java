package com.estatex.application.port.out;

import java.io.InputStream;

/**
 * Driven port — implemented in adapter-web as local filesystem storage.
 */
public interface FileStoragePort {
    String store(String filename, InputStream data, String contentType);
    void delete(String url);
}
