package com.example.spring_boot_demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RedisSetRequest(
        @NotBlank(message = "Key is required")
        String key,

        @NotBlank(message = "Value is required")
        String value,

        @Min(value = 1, message = "ttlSeconds must be >= 1")
        Long ttlSeconds
) {
}
