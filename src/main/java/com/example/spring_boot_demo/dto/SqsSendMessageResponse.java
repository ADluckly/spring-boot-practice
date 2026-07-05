package com.example.spring_boot_demo.dto;

public record SqsSendMessageResponse(String messageId, String queueUrl) {
}
