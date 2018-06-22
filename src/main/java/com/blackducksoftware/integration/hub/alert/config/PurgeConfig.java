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

import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.datasource.purge.PurgeProcessor;
import com.blackducksoftware.integration.hub.alert.datasource.purge.PurgeReader;
import com.blackducksoftware.integration.hub.alert.datasource.purge.PurgeWriter;
import com.blackducksoftware.integration.hub.alert.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.scheduled.JobScheduledTask;

@Component
public class PurgeConfig extends JobScheduledTask<PurgeReader, PurgeProcessor, PurgeWriter> {

    public static final String PURGE_STEP_NAME = "PurgeStep";
    public static final String PURGE_JOB_NAME = "PurgeJob";

    @Autowired
    public PurgeConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final NotificationManager notificationManager,
            final PlatformTransactionManager transactionManager, final TaskScheduler taskScheduler) {
        super(jobLauncher, jobBuilderFactory, stepBuilderFactory, taskExecutor, notificationManager, transactionManager, taskScheduler);
    }

    @Override
    public Step createStep(final PurgeReader reader, final PurgeProcessor processor, final PurgeWriter writer) {
        return stepBuilderFactory.get(PURGE_STEP_NAME).<List<NotificationModel>, List<NotificationModel>>chunk(1).reader(reader).processor(processor).writer(writer).taskExecutor(taskExecutor).transactionManager(transactionManager)
                       .build();
    }

    @Override
    public PurgeReader reader() {
        return createReaderWithDayOffset(1);
    }

    @Override
    public PurgeWriter writer() {
        return new PurgeWriter(notificationManager);
    }

    @Override
    public PurgeProcessor processor() {
        return new PurgeProcessor();
    }

    @Override
    public String getJobName() {
        return PURGE_JOB_NAME;
    }

    @Override
    public String getStepName() {
        return PURGE_STEP_NAME;
    }

    public PurgeReader createReaderWithDayOffset(final int dayOffset) {
        return new PurgeReader(notificationManager, dayOffset);
    }

}
