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
package com.blackducksoftware.integration.hub.alert;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.alert.channel.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.hub.HubServiceWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
@EnableScheduling
public class CommonBatchConfig {
    private final static Logger logger = LoggerFactory.getLogger(CommonBatchConfig.class);

    public static final String JOB_ID_PROPERTY_NAME = "JobID";

    private final AlertProperties alertProperties;

    @Autowired
    public CommonBatchConfig(final AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory(final PlatformTransactionManager txManager) throws Exception {
        final MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(txManager);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public JobRepository jobRepository(final MapJobRepositoryFactoryBean factory) throws Exception {
        return factory.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(final JobRepository jobRepository) {
        final SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        final TaskExecutor executor = new SyncTaskExecutor();
        return executor;
    }

    @Bean
    public HubServiceWrapper hubServiceWrapper() {
        final HubServiceWrapper wrapper = new HubServiceWrapper(alertProperties);
        try {
            wrapper.init();
        } catch (final AlertException ex) {
            logger.error("Error initializing the service wrapper", ex);
        }
        return wrapper;
    }

    @Bean
    public Gson gson(final HubServiceWrapper hubServiceWrapper) {
        if (hubServiceWrapper.getHubServicesFactory() != null) {
            return hubServiceWrapper.getHubServicesFactory().getRestConnection().gson;
        }

        return new GsonBuilder().setPrettyPrinting().create();
    }

    @Bean
    public ChannelTemplateManager channelTemplateManager(final List<AbstractJmsTemplate> templateList) {
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager();
        templateList.forEach(jmsTemplate -> {
            channelTemplateManager.addTemplate(jmsTemplate.getDestinationName(), jmsTemplate);
        });
        return channelTemplateManager;
    }
}
