package com.forehapp.store.general.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    UploadResult upload(MultipartFile file, String folder);
    void delete(String key);

    record UploadResult(String key, String url) {}
}
