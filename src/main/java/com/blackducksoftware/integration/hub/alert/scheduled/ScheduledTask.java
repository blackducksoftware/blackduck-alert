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
package com.blackducksoftware.integration.hub.alert.scheduled;

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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public abstract class ScheduledTask implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> future;

    public ScheduledTask(final TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void scheduleExecution(final String cron) {
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

}
