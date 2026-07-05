package com.example.spring_boot_demo.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_demo.dto.S3UploadResponse;
import com.example.spring_boot_demo.dto.UploadTaskResponse;
import com.example.spring_boot_demo.model.UploadTask;
import com.example.spring_boot_demo.model.UploadTaskStatus;
import com.example.spring_boot_demo.repository.UploadTaskRepository;

@Service
public class UploadWorkflowService {

    private final S3FileService s3FileService;
    private final SqsService sqsService;
    private final UploadTaskRepository uploadTaskRepository;
    private final UploadTaskCacheService uploadTaskCacheService;

    public UploadWorkflowService(
            S3FileService s3FileService,
            SqsService sqsService,
            UploadTaskRepository uploadTaskRepository,
            UploadTaskCacheService uploadTaskCacheService
    ) {
        this.s3FileService = s3FileService;
        this.sqsService = sqsService;
        this.uploadTaskRepository = uploadTaskRepository;
        this.uploadTaskCacheService = uploadTaskCacheService;
    }

    public UploadTaskResponse uploadThenQueue(MultipartFile file, String key) {
        S3UploadResponse uploadResponse = s3FileService.uploadFile(file, key);

        UploadTask task = new UploadTask(
                uploadResponse.objectKey(),
                file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        UploadTask savedTask = uploadTaskRepository.save(task);
        uploadTaskCacheService.put(toResponse(savedTask));

        String message = "{\"taskId\":" + savedTask.getId() + ",\"objectKey\":\""
                + savedTask.getObjectKey() + "\",\"status\":\"PENDING\"}";

        try {
            sqsService.sendMessage(message, 0);
        } catch (RuntimeException ex) {
            savedTask.markFailed("SQS send failed: " + ex.getMessage());
            uploadTaskRepository.save(savedTask);
            uploadTaskCacheService.put(toResponse(savedTask));
            throw ex;
        }

        return toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public UploadTaskResponse getTask(Long taskId) {
        UploadTaskResponse cached = uploadTaskCacheService.get(taskId);
        if (cached != null) {
            return cached;
        }

        UploadTask task = uploadTaskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Upload task not found: " + taskId));
        UploadTaskResponse response = toResponse(task);
        uploadTaskCacheService.put(response);
        return response;
    }

    @Transactional
    public PollingResult pollPendingAndUpdate(int maxRetryCount) {
        List<UploadTask> pendingTasks = uploadTaskRepository.findTop100ByStatusOrderByCreatedAtAsc(UploadTaskStatus.PENDING);

        int successCount = 0;
        int failedCount = 0;
        int checkedCount = 0;

        for (UploadTask task : pendingTasks) {
            checkedCount++;
            task.incrementCheckCount();

            boolean exists = s3FileService.objectExists(task.getObjectKey());
            if (exists) {
                task.markSuccess();
                uploadTaskCacheService.put(toResponse(task));
                successCount++;
                continue;
            }

            if (task.getCheckCount() >= maxRetryCount) {
                task.markFailed("S3 object not found after retries");
                uploadTaskCacheService.put(toResponse(task));
                failedCount++;
                continue;
            }

            uploadTaskCacheService.put(toResponse(task));
        }

        return new PollingResult(checkedCount, successCount, failedCount);
    }

    private UploadTaskResponse toResponse(UploadTask task) {
        return new UploadTaskResponse(
                task.getId(),
                task.getObjectKey(),
                task.getStatus(),
                task.getCheckCount(),
                task.getErrorMessage(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public record PollingResult(int checkedCount, int successCount, int failedCount) {
    }
}
