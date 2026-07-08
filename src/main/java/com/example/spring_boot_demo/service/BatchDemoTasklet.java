package com.example.spring_boot_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class BatchDemoTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(BatchDemoTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        logger.info("Running demo batch step. jobParameters={}", chunkContext.getStepContext().getJobParameters());
        return RepeatStatus.FINISHED;
    }
}
