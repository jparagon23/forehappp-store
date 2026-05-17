package com.forehapp.store.general.storage;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

public class S3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucketName;

    public S3StorageServiceImpl(String accessKey, String secretKey, String endpoint,
                                 String region, boolean pathStyle, String bucketName) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credProvider = StaticCredentialsProvider.create(credentials);
        Region awsRegion = Region.of(region);

        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credProvider)
                .forcePathStyle(pathStyle);

        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credProvider)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build());

        if (endpoint != null && !endpoint.isBlank()) {
            URI endpointUri = URI.create(endpoint);
            clientBuilder.endpointOverride(endpointUri);
            presignerBuilder.endpointOverride(endpointUri);
        }

        this.s3Client = clientBuilder.build();
        this.presigner = presignerBuilder.build();
        this.bucketName = bucketName;
    }

    @Override
    public UploadResult upload(MultipartFile file, String folder) {
        String key = folder + "/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo para subir");
        } catch (S3Exception e) {
            throw new RuntimeException("Error al subir el archivo al almacenamiento: " + e.awsErrorDetails().errorMessage());
        }
        String url = presign(key, Duration.ofDays(7));
        return new UploadResult(key, url);
    }

    @Override
    public String presign(String key, Duration expiration) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new RuntimeException("Error al eliminar el archivo del almacenamiento: " + e.awsErrorDetails().errorMessage());
        }
    }

    private String sanitize(String filename) {
        if (filename == null) return "image";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
