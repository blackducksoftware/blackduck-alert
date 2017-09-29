package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class AccumulatorListener extends JobExecutionListenerSupport {
    private final Logger logger = LoggerFactory.getLogger(AccumulatorListener.class);

    @Override
    public void afterJob(final JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("!!! JOB FINISHED!");
        }
    }
}
