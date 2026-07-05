package com.example.spring_boot_demo.dto;

import java.time.LocalDateTime;

import com.example.spring_boot_demo.model.UploadTaskStatus;

public record UploadTaskResponse(
        Long taskId,
        String objectKey,
        UploadTaskStatus status,
        Integer checkCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
