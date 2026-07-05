package com.example.spring_boot_demo.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_demo.dto.S3UploadResponse;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3FileService {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileService(
            S3Client s3Client,
            @Value("${DEMO_S3_BUCKET:}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public S3UploadResponse uploadFile(MultipartFile file, String objectKey) {
        validateBucket();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String targetKey = buildObjectKey(file, objectKey);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(targetKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            return new S3UploadResponse("Upload successful", bucket, targetKey);
        } catch (S3Exception | IOException ex) {
            throw new RuntimeException("Upload to S3 failed: " + ex.getMessage(), ex);
        }
    }

    public DownloadFileResult downloadFile(String objectKey) {
        validateBucket();

        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("Object key is required");
        }

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(getRequest);
            GetObjectResponse metadata = response.response();

            String contentType = StringUtils.hasText(metadata.contentType())
                    ? metadata.contentType()
                    : "application/octet-stream";

            return new DownloadFileResult(response.asByteArray(), objectKey, contentType);
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException("S3 object not found: " + objectKey);
        } catch (S3Exception ex) {
            throw new RuntimeException("Download from S3 failed: " + ex.getMessage(), ex);
        }
    }

    public boolean objectExists(String objectKey) {
        validateBucket();

        if (!StringUtils.hasText(objectKey)) {
            return false;
        }

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw new RuntimeException("Check S3 object existence failed: " + ex.getMessage(), ex);
        }
    }

    private void validateBucket() {
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("Missing S3 bucket config. Set DEMO_S3_BUCKET");
        }
    }

    private String buildObjectKey(MultipartFile file, String objectKey) {
        if (StringUtils.hasText(objectKey)) {
            return objectKey.trim();
        }

        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename().trim()
                : "file.bin";

        return UUID.randomUUID() + "-" + originalFilename;
    }

    public record DownloadFileResult(byte[] content, String objectKey, String contentType) {
    }
}
