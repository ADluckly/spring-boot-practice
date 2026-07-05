package com.example.spring_boot_demo.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.spring_boot_demo.dto.UploadTaskResponse;
import com.example.spring_boot_demo.model.UploadTaskStatus;

@Service
public class UploadTaskCacheService {

    private static final String KEY_PREFIX = "upload:task:";
    private static final String SEPARATOR = "|";

    private final StringRedisTemplate redisTemplate;
    private final long ttlSeconds;

    public UploadTaskCacheService(
            StringRedisTemplate redisTemplate,
            @Value("${upload.task.cache.ttl-seconds:3600}") long ttlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    public void put(UploadTaskResponse response) {
        String key = buildKey(response.taskId());

        try {
            String payload = encode(response);
            redisTemplate.opsForValue().set(key, payload, Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ignored) {
            // Cache failures should not impact the upload workflow.
        }
    }

    public UploadTaskResponse get(Long taskId) {
        String key = buildKey(taskId);

        try {
            String payload = redisTemplate.opsForValue().get(key);
            if (payload == null) {
                return null;
            }
            return decode(payload);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public void evict(Long taskId) {
        String key = buildKey(taskId);
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ignored) {
            // Cache failures should not impact the upload workflow.
        }
    }

    private String buildKey(Long taskId) {
        return KEY_PREFIX + taskId;
    }

    private String encode(UploadTaskResponse response) {
        return response.taskId() + SEPARATOR
                + nullToEmpty(response.objectKey()) + SEPARATOR
                + response.status().name() + SEPARATOR
                + response.checkCount() + SEPARATOR
                + nullToEmpty(response.errorMessage()) + SEPARATOR
                + response.createdAt() + SEPARATOR
                + response.updatedAt();
    }

    private UploadTaskResponse decode(String payload) {
        String[] parts = payload.split("\\|", -1);
        if (parts.length != 7) {
            return null;
        }

        try {
            Long taskId = Long.valueOf(parts[0]);
            String objectKey = emptyToNull(parts[1]);
            UploadTaskStatus status = UploadTaskStatus.valueOf(parts[2]);
            Integer checkCount = Integer.valueOf(parts[3]);
            String errorMessage = emptyToNull(parts[4]);
            LocalDateTime createdAt = LocalDateTime.parse(parts[5]);
            LocalDateTime updatedAt = LocalDateTime.parse(parts[6]);

            return new UploadTaskResponse(taskId, objectKey, status, checkCount, errorMessage, createdAt, updatedAt);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            return null;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.replace(SEPARATOR, " ");
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
