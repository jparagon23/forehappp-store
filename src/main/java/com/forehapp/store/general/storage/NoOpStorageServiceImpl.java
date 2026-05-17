package com.forehapp.store.general.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

public class NoOpStorageServiceImpl implements StorageService {

    @Override
    public UploadResult upload(MultipartFile file, String folder) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Storage not configured. Set AWS_ACCESS_KEY_ID environment variable.");
    }

    @Override
    public void delete(String key) {
        // no-op — nothing to delete if storage is not configured
    }

    @Override
    public String presign(String key, Duration expiration) {
        return "";
    }
}
