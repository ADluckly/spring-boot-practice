package com.example.spring_boot_demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_demo.dto.SqsDeleteMessageResponse;
import com.example.spring_boot_demo.dto.SqsMessageResponse;
import com.example.spring_boot_demo.dto.SqsSendMessageRequest;
import com.example.spring_boot_demo.dto.SqsSendMessageResponse;
import com.example.spring_boot_demo.service.SqsService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/sqs")
@Validated
public class SqsController {

    private final SqsService sqsService;

    public SqsController(SqsService sqsService) {
        this.sqsService = sqsService;
    }

    @PostMapping("/messages")
    public ResponseEntity<SqsSendMessageResponse> sendMessage(@Valid @RequestBody SqsSendMessageRequest request) {
        String messageId = sqsService.sendMessage(request.message(), request.delaySeconds());
        return ResponseEntity.ok(new SqsSendMessageResponse(messageId, sqsService.queueUrl()));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<SqsMessageResponse>> receiveMessages(
            @RequestParam(defaultValue = "1") @Min(1) @Max(10) int maxNumber,
            @RequestParam(defaultValue = "0") @Min(0) @Max(20) int waitSeconds
    ) {
        return ResponseEntity.ok(sqsService.receiveMessages(maxNumber, waitSeconds));
    }

    @DeleteMapping("/messages")
    public ResponseEntity<SqsDeleteMessageResponse> deleteMessage(@RequestParam String receiptHandle) {
        sqsService.deleteMessage(receiptHandle);
        return ResponseEntity.ok(new SqsDeleteMessageResponse("Message deleted successfully"));
    }
}
