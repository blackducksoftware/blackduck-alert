/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.scheduled;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public abstract class ScheduledTask implements Runnable {
    public static final String FORMAT_PATTERN = "MM/dd/yyy hh:mm a";
    public static final String STOP_SCHEDULE_EXPRESSION = "";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> future;

    private final String taskName;

    public ScheduledTask(final TaskScheduler taskScheduler, final String taskName) {
        this.taskScheduler = taskScheduler;
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void scheduleExecution(final String cron) {
        if (StringUtils.isNotBlank(cron)) {
            try {
                final CronTrigger cronTrigger = new CronTrigger(cron, TimeZone.getTimeZone("UTC"));
                unscheduleTask();
                logger.info("Scheduling " + this.getClass().getSimpleName() + " with cron : " + cron);
                future = taskScheduler.schedule(this, cronTrigger);
            } catch (final IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            if (future != null) {
                logger.info("Un-Scheduling " + this.getClass().getSimpleName());
                unscheduleTask();
            }
        }
    }

    public void scheduleExecutionAtFixedRate(final long period) {
        if (period > 0) {
            unscheduleTask();
            logger.info("Scheduling " + this.getClass().getSimpleName() + " with fixed rate : " + period);
            future = taskScheduler.scheduleAtFixedRate(this, period);
        } else {
            if (future != null) {
                logger.info("Un-Scheduling " + this.getClass().getSimpleName());
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
        final Optional<Long> msToNextRun = getMillisecondsToNextRun();
        if (!msToNextRun.isPresent()) {
            return Optional.empty();
        } else {
            final ZonedDateTime currentUTCTime = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime nextRunTime = currentUTCTime.plus(msToNextRun.get(), ChronoUnit.MILLIS);
            nextRunTime = nextRunTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
            final String formattedString = nextRunTime.format(DateTimeFormatter.ofPattern(FORMAT_PATTERN));
            return Optional.of(formattedString + " UTC");
        }
    }

}
