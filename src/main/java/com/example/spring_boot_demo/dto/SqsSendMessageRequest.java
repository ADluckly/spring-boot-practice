package com.example.spring_boot_demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SqsSendMessageRequest(
        @NotBlank(message = "Message is required")
        String message,

        @Min(value = 0, message = "delaySeconds must be >= 0")
        @Max(value = 900, message = "delaySeconds must be <= 900")
        Integer delaySeconds
) {
}
