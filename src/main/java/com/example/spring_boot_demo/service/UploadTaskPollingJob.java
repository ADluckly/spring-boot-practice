package com.example.spring_boot_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UploadTaskPollingJob {

    private static final Logger log = LoggerFactory.getLogger(UploadTaskPollingJob.class);

    private final UploadWorkflowService uploadWorkflowService;

    @Value("${upload.status.poller.enabled:false}")
    private boolean enabled;

    @Value("${upload.status.poller.max-retry:6}")
    private int maxRetry;

    public UploadTaskPollingJob(UploadWorkflowService uploadWorkflowService) {
        this.uploadWorkflowService = uploadWorkflowService;
    }

    @Scheduled(
            fixedDelayString = "${upload.status.poller.fixed-delay-ms:15000}",
            initialDelayString = "${upload.status.poller.initial-delay-ms:5000}"
    )
    public void pollAndUpdateStatus() {
        if (!enabled) {
            return;
        }

        try {
            UploadWorkflowService.PollingResult result = uploadWorkflowService.pollPendingAndUpdate(maxRetry);
            if (result.checkedCount() > 0) {
                log.info(
                        "Upload poll checked={}, success={}, failed={}",
                        result.checkedCount(),
                        result.successCount(),
                        result.failedCount()
                );
            }
        } catch (Exception ex) {
            log.warn("Upload status polling failed: {}", ex.getMessage());
        }
    }
}
