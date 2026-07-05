package com.example.spring_boot_demo.dto;

public record SqsMessageResponse(String messageId, String body, String receiptHandle) {
}
