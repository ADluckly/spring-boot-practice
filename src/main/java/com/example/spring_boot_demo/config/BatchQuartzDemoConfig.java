package com.example.spring_boot_demo.config;

import java.util.Date;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.JobDetail;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.spring_boot_demo.service.BatchDemoTasklet;
import com.example.spring_boot_demo.service.QuartzBatchLauncherJob;

@Configuration
public class BatchQuartzDemoConfig {

    public static final String BATCH_JOB_NAME = "demoBatchJob";
    public static final String QUARTZ_JOB_NAME = "demoBatchQuartzJob";
    public static final String QUARTZ_TRIGGER_NAME = "demoBatchQuartzTrigger";

    @Bean
    public Step demoBatchStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatchDemoTasklet batchDemoTasklet) {
        return new StepBuilder("demoBatchStep", jobRepository)
                .tasklet(batchDemoTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job demoBatchJob(JobRepository jobRepository, Step demoBatchStep) {
        return new JobBuilder(BATCH_JOB_NAME, jobRepository)
                .start(demoBatchStep)
                .build();
    }

    @Bean
    public JobDetail demoBatchQuartzJobDetail() {
        return org.quartz.JobBuilder.newJob(QuartzBatchLauncherJob.class)
                .withIdentity(QUARTZ_JOB_NAME)
                .storeDurably()
                .withDescription("Launches Spring Batch demo job on schedule")
                .build();
    }

    @Bean
    public Trigger demoBatchQuartzTrigger(JobDetail demoBatchQuartzJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(demoBatchQuartzJobDetail)
                .withIdentity(QUARTZ_TRIGGER_NAME)
                .startAt(new Date(System.currentTimeMillis() + 10_000L))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(30)
                        .repeatForever())
                .withDescription("Runs every 30 seconds")
                .build();
    }
}
