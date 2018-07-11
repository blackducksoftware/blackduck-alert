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
package com.blackducksoftware.integration.alert.config;

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

import com.blackducksoftware.integration.alert.NotificationManager;
import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.digest.DailyDigestItemProcessor;
import com.blackducksoftware.integration.alert.digest.DailyItemReader;
import com.blackducksoftware.integration.alert.digest.DigestItemProcessor;
import com.blackducksoftware.integration.alert.digest.DigestItemWriter;
import com.blackducksoftware.integration.alert.digest.DigestNotificationProcessor;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.model.NotificationModel;
import com.blackducksoftware.integration.alert.scheduled.JobScheduledTask;

@Component
public class DailyDigestBatchConfig extends JobScheduledTask<DailyItemReader, DigestItemProcessor, DigestItemWriter> {
    private static final String ACCUMULATOR_STEP_NAME = "DailyDigestBatchStep";
    private static final String ACCUMULATOR_JOB_NAME = "DailyDigestBatchJob";

    private final ChannelTemplateManager channelTemplateManager;
    private final DigestNotificationProcessor notificationProcessor;

    @Autowired
    public DailyDigestBatchConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final NotificationManager notificationManager,
            final PlatformTransactionManager transactionManager, final TaskScheduler taskScheduler, final ChannelTemplateManager channelTemplateManager, final DigestNotificationProcessor notificationProcessor) {
        super(jobLauncher, jobBuilderFactory, stepBuilderFactory, taskExecutor, notificationManager, transactionManager, taskScheduler);
        this.channelTemplateManager = channelTemplateManager;
        this.notificationProcessor = notificationProcessor;
    }

    @Override
    public Step createStep(final DailyItemReader reader, final DigestItemProcessor processor, final DigestItemWriter writer) {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<List<NotificationModel>, List<ChannelEvent>>chunk(1).reader(reader).processor(processor).writer(writer).taskExecutor(taskExecutor).transactionManager(transactionManager).build();
    }

    @Override
    public DailyItemReader reader() {
        return new DailyItemReader(notificationManager);
    }

    @Override
    public DigestItemWriter writer() {
        return new DigestItemWriter(channelTemplateManager);
    }

    @Override
    public DigestItemProcessor processor() {
        return new DailyDigestItemProcessor(notificationProcessor);
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
