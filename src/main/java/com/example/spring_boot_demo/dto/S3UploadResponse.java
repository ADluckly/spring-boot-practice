package com.example.spring_boot_demo.dto;

public record S3UploadResponse(String message, String bucket, String objectKey) {
}
