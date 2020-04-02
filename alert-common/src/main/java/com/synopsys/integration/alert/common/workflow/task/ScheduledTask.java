/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.task;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.support.CronTrigger;

public abstract class ScheduledTask implements Runnable {
    public static final String FORMAT_PATTERN = "MM/dd/yyy hh:mm a";
    public static final String STOP_SCHEDULE_EXPRESSION = "";
    public static final String EVERY_MINUTE_CRON_EXPRESSION = "0 0/1 * 1/1 * *";
    public static final Long EVERY_MINUTE_SECONDS = 60L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TaskScheduler taskScheduler;
    private final String taskName;
    private ScheduledFuture<?> future;

    public static String computeTaskName(Class<? extends ScheduledTask> clazz) {
        return String.format("Task::Class[%s]", computeFullyQualifiedName(clazz));
    }

    public static String computeFullyQualifiedName(Class<? extends ScheduledTask> clazz) {
        String packageName = clazz.getPackageName();
        String simpleClassName = clazz.getSimpleName();

        return String.format("%s.%s", packageName, simpleClassName);
    }

    public ScheduledTask(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.taskName = computeTaskName(getClass());
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskMetaData createTaskMetaData() {
        return new TaskMetaData(getTaskName(), getClass().getSimpleName(), computeFullyQualifiedName(getClass()), getFormatedNextRunTime().orElse(""), List.of());
    }

    @Override
    @Async
    public void run() {
        logger.info("### {} Task Started...", getTaskName());
        runTask();
        logger.info("### {} Task Finished", getTaskName());
    }

    public abstract void runTask();

    public void scheduleExecution(String cron) {
        if (StringUtils.isNotBlank(cron)) {
            try {
                CronTrigger cronTrigger = new CronTrigger(cron, TimeZone.getTimeZone("UTC"));
                unscheduleTask();
                logger.info("Scheduling {} with cron : {}", getTaskName(), cron);
                future = taskScheduler.schedule(this, cronTrigger);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            if (future != null) {
                logger.info("Un-Scheduling {}", getTaskName());
                unscheduleTask();
            }
        }
    }

    public void scheduleExecutionAtFixedRate(long period) {
        if (period > 0) {
            unscheduleTask();
            logger.info("Scheduling {} with fixed rate : {}", getTaskName(), period);
            future = taskScheduler.scheduleAtFixedRate(this, period);
        } else {
            if (future != null) {
                logger.info("Un-Scheduling {}", getTaskName());
                unscheduleTask();
            }
        }
    }

    private void unscheduleTask() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public Optional<Long> getMillisecondsToNextRun() {
        if (future == null || future.isCancelled() || future.isDone()) {
            return Optional.empty();
        } else {
            return Optional.of(future.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    public Optional<String> getFormatedNextRunTime() {
        Optional<Long> msToNextRun = getMillisecondsToNextRun();
        if (msToNextRun.isPresent()) {

            ZonedDateTime currentUTCTime = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime nextRunTime = currentUTCTime.plus(msToNextRun.get(), ChronoUnit.MILLIS);
            int seconds = nextRunTime.getSecond();
            if (seconds >= 30) {
                nextRunTime = nextRunTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
            } else {
                nextRunTime = nextRunTime.truncatedTo(ChronoUnit.MINUTES);
            }
            String formattedString = nextRunTime.format(DateTimeFormatter.ofPattern(FORMAT_PATTERN));
            return Optional.of(formattedString + " UTC");
        }
        return Optional.empty();
    }

}
