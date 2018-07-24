/**
 * blackduck-alert
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
package com.blackducksoftware.integration.alert.common.accumulator;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.workflow.scheduled.ScheduledTask;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public abstract class SearchIntervalAccumulator extends ScheduledTask implements Accumulator {

    public static final String ENCODING = "UTF-8";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final String cronExpression;
    private final File searchRangeFilePath;

    public SearchIntervalAccumulator(final TaskScheduler taskScheduler, final String name, final String cronExpression, final String searchStartFileDirectory) {
        super(taskScheduler);
        this.name = name;
        this.cronExpression = cronExpression;
        final String accumulatorFileName = String.format("%s-last-search.txt", name);
        this.searchRangeFilePath = new File(searchStartFileDirectory, accumulatorFileName);
        ;

    }

    @Override
    public void run() {
        accumulate();
    }

    @Override
    public void accumulate() {
        logger.info(createLoggerMessage("### Accumulator Starting Operation..."));
        try {
            if (!getSearchRangeFilePath().exists()) {
                initializeSearchRangeFile();
            }
            final Pair<Date, Date> dateRange = createDateRange(getSearchRangeFilePath());
            final Date nextSearchStartTime = accumulate(dateRange);
            final String nextSearchStartString = formatDate(nextSearchStartTime);
            logger.info(createLoggerMessage("Accumulator Next Range Start Time: {} "), nextSearchStartString);
            FileUtils.write(getSearchRangeFilePath(), nextSearchStartString, ENCODING);
        } catch (final IOException | AlertException ex) {
            logger.error(createLoggerMessage("Error occurred accumulating data! "), ex);
        } finally {
            final Optional<Long> nextRun = getMillisecondsToNextRun();
            if (nextRun.isPresent()) {
                final Long seconds = TimeUnit.MILLISECONDS.toSeconds(nextRun.get());
                logger.debug(createLoggerMessage("Accumulator next run: {} seconds"), seconds);
            }
            logger.info(createLoggerMessage("### Accumulator Finished Operation."));
        }
    }

    public void initializeSearchRangeFile() throws IOException {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        FileUtils.write(getSearchRangeFilePath(), formatDate(date), ENCODING);
    }

    public String formatDate(final Date date) {
        return RestConnection.formatDate(date);
    }

    public String createLoggerMessage(final String messageFormat) {
        return String.format("[ %s ] %s", getName(), messageFormat);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        this.scheduleExecution(cronExpression);
    }

    @Override
    public void stop() {
        this.scheduleExecution(STOP_SCHEDULE_EXPRESSION);
    }

    public File getSearchRangeFilePath() {
        return searchRangeFilePath;
    }

    protected abstract Pair<Date, Date> createDateRange(final File lastRunFile) throws AlertException;

    protected abstract Date accumulate(Pair<Date, Date> dateRange) throws AlertException;

}

