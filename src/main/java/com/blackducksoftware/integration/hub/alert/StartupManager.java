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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.purge.PurgeProcessor;
import com.blackducksoftware.integration.hub.alert.datasource.purge.PurgeReader;
import com.blackducksoftware.integration.hub.alert.datasource.purge.PurgeWriter;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingRepositoryWrapper;

@Component
@Transactional
public class StartupManager {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    private final GlobalSchedulingRepositoryWrapper globalSchedulingRepository;
    private final GlobalProperties globalProperties;
    private final AccumulatorConfig accumulatorConfig;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;
    private final PurgeConfig purgeConfig;

    @Value("${logging.level.com.blackducksoftware.integration:}")
    String loggingLevel;

    @Autowired
    public StartupManager(final GlobalSchedulingRepositoryWrapper globalSchedulingRepository, final GlobalProperties globalProperties, final AccumulatorConfig accumulatorConfig, final DailyDigestBatchConfig dailyDigestBatchConfig,
            final PurgeConfig purgeConfig) {
        this.globalSchedulingRepository = globalSchedulingRepository;
        this.globalProperties = globalProperties;
        this.accumulatorConfig = accumulatorConfig;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
        this.purgeConfig = purgeConfig;
    }

    public void startup() {
        logger.info("Hub Alert Starting...");
        logConfiguration();

        initializeCronJobs();
        CompletableFuture.supplyAsync(this::purgeOldData);
    }

    public void logConfiguration() {
        final GlobalHubConfigEntity globalHubConfig = globalProperties.getHubConfig();
        final boolean authenticatedProxy = StringUtils.isNotBlank(globalProperties.getHubProxyPassword());
        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Logging level:           {}", loggingLevel);
        logger.info("Hub URL:                 {}", globalProperties.getHubUrl());
        logger.info("Hub Proxy Host:          {}", globalProperties.getHubProxyHost());
        logger.info("Hub Proxy Port:          {}", globalProperties.getHubProxyPort());
        logger.info("Hub Proxy Authenticated: {}", authenticatedProxy);
        logger.info("Hub Proxy User:          {}", globalProperties.getHubProxyUsername());

        if (globalHubConfig != null) {
            logger.info("Hub API Token:           **********");
            logger.info("Hub Timeout:             {}", globalHubConfig.getHubTimeout());
        }
        logger.info("----------------------------------------");
    }

    public void initializeCronJobs() {
        final List<GlobalSchedulingConfigEntity> globalSchedulingConfigs = globalSchedulingRepository.findAll();
        String dailyDigestHourOfDay = null;
        String purgeDataFrequencyDays = null;
        if (!globalSchedulingConfigs.isEmpty() && globalSchedulingConfigs.get(0) != null) {
            final GlobalSchedulingConfigEntity globalSchedulingConfig = globalSchedulingConfigs.get(0);
            dailyDigestHourOfDay = globalSchedulingConfig.getDailyDigestHourOfDay();
            purgeDataFrequencyDays = globalSchedulingConfig.getPurgeDataFrequencyDays();
        } else {
            dailyDigestHourOfDay = "0";
            purgeDataFrequencyDays = "3";
            final GlobalSchedulingConfigEntity globalSchedulingConfig = new GlobalSchedulingConfigEntity(dailyDigestHourOfDay, purgeDataFrequencyDays);
            final GlobalSchedulingConfigEntity savedGlobalSchedulingConfig = globalSchedulingRepository.save(globalSchedulingConfig);
            logger.info(savedGlobalSchedulingConfig.toString());
        }
        scheduleCronJobs(dailyDigestHourOfDay, purgeDataFrequencyDays);
    }

    public void scheduleCronJobs(final String dailyDigestHourOfDay, final String purgeDataFrequencyDays) {
        accumulatorConfig.scheduleJobExecution("0 0/1 * 1/1 * *");
        final Long seconds = TimeUnit.MILLISECONDS.toSeconds(accumulatorConfig.getMillisecondsToNextRun());
        logger.info("Accumulator next run: {} seconds", seconds);

        final String dailyDigestCron = String.format("0 0 %s 1/1 * ?", dailyDigestHourOfDay);
        final String purgeDataCron = String.format("0 0 0 1/%s * ?", purgeDataFrequencyDays);
        dailyDigestBatchConfig.scheduleJobExecution(dailyDigestCron);
        purgeConfig.scheduleJobExecution(purgeDataCron);

        logger.info("Daily Digest next run:     {}", dailyDigestBatchConfig.getFormatedNextRunTime());
        logger.info("Purge Old Data next run:   {}", purgeConfig.getFormatedNextRunTime());
    }

    private Boolean purgeOldData() {
        try {
            logger.info("Begin startup purge of old data");
            final PurgeReader reader = purgeConfig.reader();
            final PurgeProcessor processor = purgeConfig.processor();
            final PurgeWriter writer = purgeConfig.writer();

            final List<NotificationModel> purgeData = reader.read();
            final List<NotificationModel> processedData = processor.process(purgeData);
            final List<List<NotificationModel>> dataToDelete = new ArrayList<>();
            dataToDelete.add(processedData);
            writer.write(dataToDelete);
            return Boolean.TRUE;
        } catch (final Exception ex) {
            logger.error("Error occurred puring data on startup", ex);
            return Boolean.FALSE;
        } finally {
            logger.info("Finished startup purge of old data");
        }
    }
}
