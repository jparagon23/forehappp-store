package com.forehapp.store.general.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface StorageService {
    UploadResult upload(MultipartFile file, String folder);
    void delete(String key);
    String presign(String key, Duration expiration);

    record UploadResult(String key, String url) {}
}
