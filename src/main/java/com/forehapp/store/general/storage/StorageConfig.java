package com.forehapp.store.general.storage;

import org.springframework.beans.factory.annotation.Value;
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
    public StorageService storageService(
            @Value("${AWS_ACCESS_KEY_ID:}") String accessKey,
            @Value("${AWS_SECRET_ACCESS_KEY:}") String secretKey,
            @Value("${AWS_ENDPOINT_URL:}") String endpoint,
            @Value("${AWS_DEFAULT_REGION:us-east-1}") String region,
            @Value("${STORAGE_PATH_STYLE:false}") boolean pathStyle,
            @Value("${AWS_S3_BUCKET_NAME:local-bucket}") String bucketName,
            @Value("${STORAGE_PUBLIC_URL:http://localhost}") String publicUrl) {

        if (accessKey.isBlank()) {
            return new NoOpStorageServiceImpl();
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(pathStyle);

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        S3Client s3Client = builder.build();
        return new S3StorageServiceImpl(s3Client, bucketName, publicUrl);
    }
}
