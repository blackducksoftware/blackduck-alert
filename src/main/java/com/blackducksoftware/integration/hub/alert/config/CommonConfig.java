/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.config;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.alert.NotificationManager;

public abstract class CommonConfig<R extends ItemReader<?>, P extends ItemProcessor<?, ?>, W extends ItemWriter<?>> implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(CommonConfig.class);
    public static final String JOB_ID_PROPERTY_NAME = "JobID";

    protected final SimpleJobLauncher jobLauncher;
    protected final JobBuilderFactory jobBuilderFactory;
    protected final StepBuilderFactory stepBuilderFactory;
    protected final TaskExecutor taskExecutor;
    protected final NotificationManager notificationManager;
    protected final PlatformTransactionManager transactionManager;
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> future;

    public CommonConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor,
            final NotificationManager notificationManager, final PlatformTransactionManager transactionManager, final TaskScheduler taskScheduler) {
        this.jobLauncher = jobLauncher;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.taskExecutor = taskExecutor;
        this.notificationManager = notificationManager;
        this.transactionManager = transactionManager;
        this.taskScheduler = taskScheduler;
    }

    public void scheduleJobExecution(final String cron) {
        if (StringUtils.isNotBlank(cron)) {
            try {
                final CronTrigger cronTrigger = new CronTrigger(cron, TimeZone.getTimeZone("UTC"));
                if (future != null) {
                    future.cancel(false);
                }
                logger.info("Scheduling " + this.getClass().getSimpleName() + " with cron : " + cron);
                future = taskScheduler.schedule(this, cronTrigger);
            } catch (final IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            if (future != null) {
                logger.info("Un-Scheduling " + this.getClass().getSimpleName());
                future.cancel(false);
            }
        }
    }

    public Long getMillisecondsToNextRun() {
        if (future == null || future.isCancelled() || future.isDone()) {
            return null;
        } else {
            return future.getDelay(TimeUnit.MILLISECONDS);
        }
    }

    public String getFormatedNextRunTime() {
        final Long msToNextRun = getMillisecondsToNextRun();
        if (msToNextRun == null) {
            return null;
        } else {
            final ZonedDateTime currentUTCTime = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime nextRunTime = currentUTCTime.plus(msToNextRun, ChronoUnit.MILLIS);
            nextRunTime = nextRunTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
            final String formattedString = nextRunTime.format(DateTimeFormatter.ofPattern("MM/dd/yyy hh:mm a"));
            return formattedString + " UTC";
        }
    }

    @Override
    public void run() {
        try {
            final JobExecution execution = createJobExecution();
            logger.info("Job finished with status : " + execution.getExitStatus().getExitDescription() + ", code : " + execution.getExitStatus().getExitCode());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public JobExecution createJobExecution() throws Exception {
        final JobParameters param = new JobParametersBuilder().addString(JOB_ID_PROPERTY_NAME, createJobID()).toJobParameters();
        final JobExecution execution = jobLauncher.run(createJob(reader(), processor(), writer()), param);
        return execution;
    }

    public Job createJob(final R reader, final P processor, final W writer) {
        return jobBuilderFactory.get(getJobName()).incrementer(new RunIdIncrementer()).flow(createStep(reader, processor, writer)).end().build();
    }

    public String createJobID() {
        return String.format("%s-%d", getJobName(), System.currentTimeMillis());
    }

    public abstract Step createStep(R reader, P processor, W writer);

    public abstract R reader();

    public abstract W writer();

    public abstract P processor();

    public abstract String getJobName();

    public abstract String getStepName();
}
