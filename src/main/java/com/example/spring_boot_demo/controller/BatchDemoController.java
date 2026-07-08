package com.example.spring_boot_demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_demo.config.BatchQuartzDemoConfig;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/batch-demo")
@Validated
public class BatchDemoController {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job batchJob;

    public BatchDemoController(JobLauncher jobLauncher,
            JobExplorer jobExplorer,
            @Qualifier("demoBatchJob") Job batchJob) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.batchJob = batchJob;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runNow() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("requestedAt", System.currentTimeMillis())
                .addString("requestId", UUID.randomUUID().toString())
                .addString("trigger", "manual-api")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(batchJob, parameters);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jobName", execution.getJobInstance().getJobName());
        response.put("executionId", execution.getId());
        response.put("status", execution.getStatus().toString());
        response.put("parameters", execution.getJobParameters().parameters());

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/executions")
    public ResponseEntity<List<Map<String, Object>>> recentExecutions(
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        List<JobInstance> instances = jobExplorer.getJobInstances(BatchQuartzDemoConfig.BATCH_JOB_NAME, 0, limit);
        List<JobExecution> executions = new ArrayList<>();

        for (JobInstance instance : instances) {
            executions.addAll(jobExplorer.getJobExecutions(instance));
        }

        executions.sort(Comparator.comparing(JobExecution::getCreateTime).reversed());

        List<Map<String, Object>> response = executions.stream()
                .limit(limit)
                .map(this::toExecutionMap)
                .toList();

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toExecutionMap(JobExecution execution) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("executionId", execution.getId());
        item.put("jobInstanceId", execution.getJobInstance().getInstanceId());
        item.put("status", execution.getStatus().toString());
        item.put("createTime", execution.getCreateTime());
        item.put("startTime", execution.getStartTime());
        item.put("endTime", execution.getEndTime());
        item.put("parameters", execution.getJobParameters().parameters());
        return item;
    }
}
