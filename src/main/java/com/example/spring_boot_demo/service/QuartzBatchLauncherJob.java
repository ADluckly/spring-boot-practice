package com.example.spring_boot_demo.service;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.spring_boot_demo.config.BatchQuartzDemoConfig;

@Component
@DisallowConcurrentExecution
public class QuartzBatchLauncherJob implements org.quartz.Job {

    private final JobLauncher jobLauncher;
    private final Job batchJob;

    public QuartzBatchLauncherJob(JobLauncher jobLauncher,
            @Qualifier("demoBatchJob") Job batchJob) {
        this.jobLauncher = jobLauncher;
        this.batchJob = batchJob;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("scheduledAt", System.currentTimeMillis())
                .addString("trigger", BatchQuartzDemoConfig.QUARTZ_TRIGGER_NAME)
                .toJobParameters();

        try {
            jobLauncher.run(batchJob, parameters);
        } catch (Exception exception) {
            throw new JobExecutionException("Failed to launch batch job from Quartz", exception);
        }
    }
}
