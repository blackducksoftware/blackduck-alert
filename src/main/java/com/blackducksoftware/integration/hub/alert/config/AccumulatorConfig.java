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

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.alert.HubServiceWrapper;
import com.blackducksoftware.integration.hub.alert.batch.accumulator.AccumulatorProcessor;
import com.blackducksoftware.integration.hub.alert.batch.accumulator.AccumulatorReader;
import com.blackducksoftware.integration.hub.alert.batch.accumulator.AccumulatorWriter;
import com.blackducksoftware.integration.hub.alert.datasource.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.service.HubResponseService;

@Configuration
@EnableBatchProcessing
public class AccumulatorConfig extends CommonConfig {
    private static final String ACCUMULATOR_STEP_NAME = "AccumulatorStep";
    private static final String ACCUMULATOR_JOB_NAME = "AccumulatorJob";

    private final HubServiceWrapper hubServiceWrapper;

    @Autowired
    public AccumulatorConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final NotificationRepository notificationRepository,
            final PlatformTransactionManager transactionManager, final HubServiceWrapper hubServiceWrapper) {
        super(jobLauncher, jobBuilderFactory, stepBuilderFactory, taskExecutor, notificationRepository, transactionManager);
        this.hubServiceWrapper = hubServiceWrapper;

    }

    @Override
    @Scheduled(cron = "#{@accumulatorCronExpression}")
    public JobExecution perform() throws Exception {
        return super.perform();
    }

    @Override
    public Step accumulatorStep() {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<NotificationResults, DBStoreEvent> chunk(1).reader(getReader()).processor(getProcessor()).writer(getWriter()).taskExecutor(taskExecutor).transactionManager(transactionManager)
                .build();
    }

    public NotificationItemProcessor getNotificationProcessor() {
        final HubResponseService hubResponseService = hubServiceWrapper.getHubServicesFactory().createHubResponseService();
        final MetaService metaService = hubServiceWrapper.getHubServicesFactory().createMetaService();
        final VulnerabilityRequestService vulnerabilityRequestService = hubServiceWrapper.getHubServicesFactory().createVulnerabilityRequestService();
        final NotificationItemProcessor notificationProcessor = new NotificationItemProcessor(hubResponseService, vulnerabilityRequestService, metaService);
        return notificationProcessor;
    }

    @Override
    public AccumulatorReader getReader() {
        return new AccumulatorReader(hubServiceWrapper);
    }

    @Override
    public AccumulatorWriter getWriter() {
        return new AccumulatorWriter(notificationRepository);
    }

    @Override
    public AccumulatorProcessor getProcessor() {
        return new AccumulatorProcessor(getNotificationProcessor());
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
