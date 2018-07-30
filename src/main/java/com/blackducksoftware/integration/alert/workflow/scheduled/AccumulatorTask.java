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
package com.blackducksoftware.integration.alert.workflow.scheduled;

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

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.provider.blackduck.accumulator.BlackDuckAccumulatorProcessor;
import com.blackducksoftware.integration.alert.provider.blackduck.accumulator.BlackDuckAccumulatorReader;
import com.blackducksoftware.integration.alert.provider.blackduck.accumulator.BlackDuckAccumulatorWriter;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;

@Component
public class AccumulatorTask extends JobScheduledTask<BlackDuckAccumulatorReader, BlackDuckAccumulatorProcessor, BlackDuckAccumulatorWriter> {
    private static final String ACCUMULATOR_STEP_NAME = "AccumulatorStep";
    private static final String ACCUMULATOR_JOB_NAME = "AccumulatorJob";

    private final BlackDuckProperties blackDuckProperties;
    private final List<NotificationTypeProcessor> processorList;
    private final ContentConverter contentConverter;

    @Autowired
    public AccumulatorTask(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final NotificationManager notificationManager,
            final PlatformTransactionManager transactionManager, final BlackDuckProperties blackDuckProperties, final TaskScheduler taskScheduler,
            final List<NotificationTypeProcessor> processorList, final ContentConverter contentConverter) {
        super(jobLauncher, jobBuilderFactory, stepBuilderFactory, taskExecutor, notificationManager, transactionManager, taskScheduler);
        this.blackDuckProperties = blackDuckProperties;
        this.processorList = processorList;
        this.contentConverter = contentConverter;
    }

    @Override
    public Step createStep(final BlackDuckAccumulatorReader reader, final BlackDuckAccumulatorProcessor processor, final BlackDuckAccumulatorWriter writer) {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<NotificationDetailResults, AlertEvent>chunk(1).reader(reader).processor(processor).writer(writer).taskExecutor(taskExecutor).transactionManager(transactionManager)
                .build();
    }

    @Override
    public BlackDuckAccumulatorReader reader() {
        return new BlackDuckAccumulatorReader(blackDuckProperties);
    }

    @Override
    public BlackDuckAccumulatorWriter writer() {
        return new BlackDuckAccumulatorWriter(notificationManager, contentConverter);
    }

    @Override
    public BlackDuckAccumulatorProcessor processor() {
        return new BlackDuckAccumulatorProcessor(blackDuckProperties, processorList, contentConverter);
    }

    @Override
    public String getJobName() {
        return ACCUMULATOR_JOB_NAME;
    }

    @Override
    public String getStepName() {
        return ACCUMULATOR_STEP_NAME;
    }

}
