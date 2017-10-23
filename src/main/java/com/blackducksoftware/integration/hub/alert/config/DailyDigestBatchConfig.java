/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.alert.digest.DailyItemReader;
import com.blackducksoftware.integration.hub.alert.digest.DigestItemProcessor;
import com.blackducksoftware.integration.hub.alert.digest.DigestItemWriter;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.google.gson.Gson;

@Configuration
public class DailyDigestBatchConfig extends CommonConfig<DailyItemReader, DigestItemProcessor, DigestItemWriter> {
    private static final String ACCUMULATOR_STEP_NAME = "DailyDigestBatchStep";
    private static final String ACCUMULATOR_JOB_NAME = "DailyDigestBatchJob";

    private final ChannelTemplateManager channelTemplateManager;
    private final Gson gson;

    @Autowired
    public DailyDigestBatchConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor,
            final NotificationRepository notificationRepository, final PlatformTransactionManager transactionManager, final ChannelTemplateManager channelTemplateManager, final Gson gson) {
        super(jobLauncher, jobBuilderFactory, stepBuilderFactory, taskExecutor, notificationRepository, transactionManager);
        this.channelTemplateManager = channelTemplateManager;
        this.gson = gson;
    }

    @Override
    @Scheduled(cron = "#{@dailyDigestCronExpression}", zone = "UTC")
    public JobExecution createJobExecution() throws Exception {
        return super.createJobExecution();
    }

    @Override
    public Step createStep(final DailyItemReader reader, final DigestItemProcessor processor, final DigestItemWriter writer) {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<List<NotificationEntity>, List<AbstractChannelEvent>> chunk(1).reader(reader).processor(processor).writer(writer).taskExecutor(taskExecutor)
                .transactionManager(transactionManager).build();
    }

    @Override
    public DailyItemReader reader() {
        return new DailyItemReader(notificationRepository);
    }

    @Override
    public DigestItemWriter writer() {
        return new DigestItemWriter(channelTemplateManager, gson);
    }

    @Override
    public DigestItemProcessor processor() {
        return new DigestItemProcessor();
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
