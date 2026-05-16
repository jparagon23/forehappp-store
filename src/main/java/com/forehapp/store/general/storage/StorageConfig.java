package com.forehapp.store.general.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService noOpStorageService() {
        return new NoOpStorageServiceImpl();
    }

    @Value("${storage.access-key:}")
    private String accessKey;

    @Value("${storage.secret-key:}")
    private String secretKey;

    @Value("${storage.endpoint:}")
    private String endpoint;

    @Value("${storage.region:us-east-1}")
    private String region;

    @Value("${storage.path-style:false}")
    private boolean pathStyle;

    @Bean
    @ConditionalOnProperty(name = "storage.access-key", matchIfMissing = false)
    public S3Client s3Client() {
        if (accessKey == null || accessKey.isBlank()) return null;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(pathStyle);

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}
