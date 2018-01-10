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
package com.blackducksoftware.integration.hub.alert;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@EnableAutoConfiguration(exclude = { BatchAutoConfiguration.class })
@EnableJpaRepositories(basePackages = { "com.blackducksoftware.integration.hub.alert.**.repository" })
@EnableTransactionManagement
@EnableBatchProcessing
@EnableScheduling
@EnableJms
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
@ComponentScan(basePackages = { "com.blackducksoftware.integration.hub.alert.web.**", "com.blackducksoftware.integration.hub.alert", "com.blackducksoftware.integration.hub.alert.config" })
public class Application {
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private GlobalProperties globalProperties;
    @Autowired
    private AccumulatorConfig accumulatorConfig;
    @Autowired
    private DailyDigestBatchConfig dailyDigestBatchConfig;
    @Autowired
    private PurgeConfig purgeConfig;
    @Value("${logging.level.com.blackducksoftware.integration:}")
    String loggingLevel;

    @PostConstruct
    void init() {
        logger.info("Hub Alert Starting...");
        final GlobalHubConfigEntity globalHubConfig = globalProperties.getHubConfig();
        final GlobalSchedulingConfigEntity globalSchedulingConfig = globalProperties.getSchedulingConfig();
        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Logging level: {}", loggingLevel);
        logger.info("Hub URL:            {}", globalProperties.getHubUrl());
        logger.info("Hub Proxy Host:     {}", globalProperties.getHubProxyHost());
        logger.info("Hub Proxy Port:     {}", globalProperties.getHubProxyPort());
        logger.info("Hub Proxy User:     {}", globalProperties.getHubProxyUsername());
        logger.info("Hub Proxy Password: **********");

        if (globalHubConfig != null) {
            logger.info("Hub API Key:        **********");
            logger.info("Hub Timeout:        {}", globalHubConfig.getHubTimeout());
        }
        logger.info("----------------------------------------");

        if (globalSchedulingConfig != null) {
            logger.info("Accumulator Cron Expression:      {}", globalSchedulingConfig.getAccumulatorCron());
            logger.info("Daily Digest Cron Expression:     {}", globalSchedulingConfig.getDailyDigestCron());
            logger.info("Purge Old Data Cron Expression:   {}", globalSchedulingConfig.getPurgeDataCron());
            accumulatorConfig.scheduleJobExecution(globalSchedulingConfig.getAccumulatorCron());
            dailyDigestBatchConfig.scheduleJobExecution(globalSchedulingConfig.getDailyDigestCron());
            purgeConfig.scheduleJobExecution(globalSchedulingConfig.getPurgeDataCron());
        }
    }

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
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
    public TaskScheduler taskScheduler() throws Exception {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        final TaskExecutor executor = new SyncTaskExecutor();
        return executor;
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder().setDateFormat(RestConnection.JSON_DATE_FORMAT).create();
    }
}
