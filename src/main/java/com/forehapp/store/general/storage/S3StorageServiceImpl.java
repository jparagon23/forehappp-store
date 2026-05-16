package com.forehapp.store.general.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnBean(software.amazon.awssdk.services.s3.S3Client.class)
public class S3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;

    @Value("${storage.bucket-name}")
    private String bucketName;

    @Value("${storage.public-url}")
    private String publicUrl;

    public S3StorageServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
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
        String url = publicUrl.replaceAll("/+$", "") + "/" + key;
        return new UploadResult(key, url);
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
