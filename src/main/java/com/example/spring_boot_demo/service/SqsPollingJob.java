package com.example.spring_boot_demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.spring_boot_demo.dto.SqsMessageResponse;

@Component
public class SqsPollingJob {

    private static final Logger log = LoggerFactory.getLogger(SqsPollingJob.class);

    private final SqsService sqsService;

    @Value("${sqs.poller.enabled:false}")
    private boolean enabled;

    @Value("${sqs.poller.max-number:5}")
    private int maxNumber;

    @Value("${sqs.poller.wait-seconds:0}")
    private int waitSeconds;

    @Value("${sqs.poller.auto-delete:false}")
    private boolean autoDelete;

    public SqsPollingJob(SqsService sqsService) {
        this.sqsService = sqsService;
    }

    @Scheduled(
            fixedDelayString = "${sqs.poller.fixed-delay-ms:10000}",
            initialDelayString = "${sqs.poller.initial-delay-ms:3000}"
    )
    public void poll() {
        if (!enabled) {
            return;
        }

        try {
            List<SqsMessageResponse> messages = sqsService.receiveMessages(maxNumber, waitSeconds);
            if (messages.isEmpty()) {
                return;
            }

            for (SqsMessageResponse message : messages) {
                log.info("SQS polled message id={}, body={}", message.messageId(), abbreviate(message.body()));

                if (autoDelete) {
                    sqsService.deleteMessage(message.receiptHandle());
                    log.info("SQS auto deleted message id={}", message.messageId());
                }
            }
        } catch (Exception ex) {
            // Keep scheduler alive even if one polling round fails.
            log.warn("SQS polling failed: {}", ex.getMessage());
        }
    }

    private String abbreviate(String body) {
        if (body == null) {
            return "";
        }

        int maxLength = 120;
        if (body.length() <= maxLength) {
            return body;
        }

        return body.substring(0, maxLength) + "...";
    }
}
