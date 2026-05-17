package com.forehapp.store.general.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public StorageService storageService(
            @Value("${AWS_ACCESS_KEY_ID:}") String accessKey,
            @Value("${AWS_SECRET_ACCESS_KEY:}") String secretKey,
            @Value("${AWS_ENDPOINT_URL:}") String endpoint,
            @Value("${AWS_DEFAULT_REGION:us-east-1}") String region,
            @Value("${STORAGE_PATH_STYLE:false}") boolean pathStyle,
            @Value("${AWS_S3_BUCKET_NAME:local-bucket}") String bucketName) {

        if (accessKey.isBlank()) {
            return new NoOpStorageServiceImpl();
        }

        return new S3StorageServiceImpl(accessKey, secretKey, endpoint, region, pathStyle, bucketName);
    }
}
