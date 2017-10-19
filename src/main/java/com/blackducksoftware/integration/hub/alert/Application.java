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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@EnableAutoConfiguration(exclude = { BatchAutoConfiguration.class })
@SpringBootApplication
// @ComponentScan(basePackages = { "com.blackducksoftware.integration.hub.alert" })
public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private AlertProperties alertProperties;

    @Autowired
    private HubServiceWrapper hubServiceWrapper;

    @Autowired
    private List<AbstractJmsTemplate> templateList;

    @PostConstruct
    void init() {
        logger.info("Hub Alert Starting...");
        logger.info("Alert Configuration: ");
        logger.info("Hub URL:            {}", alertProperties.getHubUrl());
        logger.info("Hub Username:       {}", alertProperties.getHubUsername());
        logger.info("Hub Password:       **********");
        logger.info("Hub Timeout:        {}", alertProperties.getHubTimeout());
        logger.info("Hub Proxy Host:     {}", alertProperties.getHubProxyHost());
        logger.info("Hub Proxy Port:     {}", alertProperties.getHubProxyPort());
        logger.info("Hub Proxy User:     {}", alertProperties.getHubProxyUsername());
        logger.info("Hub Proxy Password: **********");

        try {
            hubServiceWrapper.init();
            final HubVersionRequestService versionRequestService = hubServiceWrapper.getHubServicesFactory().createHubVersionRequestService();
            final String hubVersion = versionRequestService.getHubVersion();
            logger.info("Hub Version: {}", hubVersion);
            logger.info("Cron Expression: {}", alertProperties.getAccumulatorCron());
        } catch (final IntegrationException ex) {
            logger.error("Error occurred initializing hub alert", ex);
        }
    }

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }

    @Bean
    public String accumulatorCronExpression() {
        return alertProperties.getAccumulatorCron();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory() throws Exception {
        final MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(transactionManager());
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        return mapJobRepositoryFactory().getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher() throws Exception {
        final SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository());
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
    public Gson gson() {
        return new GsonBuilder().setDateFormat(RestConnection.JSON_DATE_FORMAT).create();
    }

    @Bean
    public ChannelTemplateManager channelTemplateManager() {
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager();
        templateList.forEach(jmsTemplate -> {
            channelTemplateManager.addTemplate(jmsTemplate.getDestinationName(), jmsTemplate);
        });
        return channelTemplateManager;
    }

}
