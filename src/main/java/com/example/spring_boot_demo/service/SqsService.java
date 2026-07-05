package com.example.spring_boot_demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.spring_boot_demo.dto.SqsMessageResponse;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class SqsService {

    private final SqsClient sqsClient;
    private final String queueName;
    private final boolean autoCreateQueue;

    private String resolvedQueueUrl;

    public SqsService(
            SqsClient sqsClient,
            @Value("${DEMO_SQS_QUEUE_URL:}") String queueUrl,
            @Value("${DEMO_SQS_QUEUE_NAME:}") String queueName,
            @Value("${DEMO_SQS_AUTO_CREATE:false}") boolean autoCreateQueue
    ) {
        this.sqsClient = sqsClient;
        this.queueName = queueName;
        this.autoCreateQueue = autoCreateQueue;
        this.resolvedQueueUrl = queueUrl;
    }

    public String sendMessage(String body, Integer delaySeconds) {
        String targetQueueUrl = resolveQueueUrl();

        int delay = delaySeconds == null ? 0 : delaySeconds;

        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(targetQueueUrl)
                    .messageBody(body)
                    .delaySeconds(delay)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(request);
            return response.messageId();
        } catch (SqsException ex) {
            throw new RuntimeException("Send message to SQS failed: " + ex.getMessage(), ex);
        }
    }

    public List<SqsMessageResponse> receiveMessages(int maxNumber, int waitSeconds) {
        String targetQueueUrl = resolveQueueUrl();

        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(targetQueueUrl)
                    .maxNumberOfMessages(maxNumber)
                    .waitTimeSeconds(waitSeconds)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();

            return messages.stream()
                    .map(message -> new SqsMessageResponse(message.messageId(), message.body(), message.receiptHandle()))
                    .toList();
        } catch (SqsException ex) {
            throw new RuntimeException("Receive messages from SQS failed: " + ex.getMessage(), ex);
        }
    }

    public void deleteMessage(String receiptHandle) {
        String targetQueueUrl = resolveQueueUrl();

        if (!StringUtils.hasText(receiptHandle)) {
            throw new IllegalArgumentException("receiptHandle is required");
        }

        try {
            DeleteMessageRequest request = DeleteMessageRequest.builder()
                    .queueUrl(targetQueueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(request);
        } catch (SqsException ex) {
            throw new RuntimeException("Delete message from SQS failed: " + ex.getMessage(), ex);
        }
    }

    public String queueUrl() {
        return resolveQueueUrl();
    }

    private String resolveQueueUrl() {
        if (StringUtils.hasText(resolvedQueueUrl)) {
            return resolvedQueueUrl;
        }

        if (!StringUtils.hasText(queueName)) {
            throw new IllegalStateException(
                    "Missing SQS queue config. Set DEMO_SQS_QUEUE_URL or DEMO_SQS_QUEUE_NAME"
            );
        }

        try {
            resolvedQueueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
            return resolvedQueueUrl;
        } catch (QueueDoesNotExistException ex) {
            if (!autoCreateQueue) {
                throw new IllegalStateException(
                        "SQS queue does not exist: " + queueName + ". Set DEMO_SQS_AUTO_CREATE=true to auto-create"
                );
            }

            resolvedQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build()).queueUrl();
            return resolvedQueueUrl;
        } catch (SqsException ex) {
            throw new RuntimeException("Resolve SQS queue URL failed: " + ex.getMessage(), ex);
        }
    }
}
