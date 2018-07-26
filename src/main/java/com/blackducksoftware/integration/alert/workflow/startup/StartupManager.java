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
package com.blackducksoftware.integration.alert.workflow.startup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.transaction.Transactional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.accumulator.Accumulator;
import com.blackducksoftware.integration.alert.common.descriptor.ProviderDescriptor;
import com.blackducksoftware.integration.alert.common.enumeration.AlertEnvironment;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.config.PurgeConfig;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.purge.PurgeProcessor;
import com.blackducksoftware.integration.alert.database.purge.PurgeReader;
import com.blackducksoftware.integration.alert.database.purge.PurgeWriter;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingRepository;
import com.blackducksoftware.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.blackducksoftware.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.blackducksoftware.integration.alert.workflow.scheduled.frequency.OnDemandTask;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.service.model.HubServerVerifier;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;

@Component
@Transactional
public class StartupManager {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    private final GlobalSchedulingRepository globalSchedulingRepository;
    private final GlobalProperties globalProperties;
    private final DailyTask dailyTask;
    private final OnDemandTask onDemandTask;
    private final PurgeConfig purgeConfig;
    private final PhoneHomeTask phoneHomeTask;
    private final AlertStartupInitializer alertStartupInitializer;
    private final List<ProviderDescriptor> providerDescriptorList;

    @Value("${logging.level.com.blackducksoftware.integration:}")
    String loggingLevel;

    @Autowired
    public StartupManager(final GlobalSchedulingRepository globalSchedulingRepository, final GlobalProperties globalProperties,
            final DailyTask dailyTask, final OnDemandTask onDemandTask,
            final PurgeConfig purgeConfig, final PhoneHomeTask phoneHometask, final AlertStartupInitializer alertStartupInitializer, final List<ProviderDescriptor> providerDescriptorList) {
        this.globalSchedulingRepository = globalSchedulingRepository;
        this.globalProperties = globalProperties;
        this.dailyTask = dailyTask;
        this.onDemandTask = onDemandTask;
        this.purgeConfig = purgeConfig;
        this.phoneHomeTask = phoneHometask;
        this.alertStartupInitializer = alertStartupInitializer;
        this.providerDescriptorList = providerDescriptorList;
    }

    public void startup() {
        logger.info("Hub Alert Starting...");
        initializeChannelPropertyManagers();
        logConfiguration();
        listProperties();
        validateProviders();
        initializeCronJobs();
        startAccumulators();
    }

    public void logConfiguration() {
        final boolean authenticatedProxy = StringUtils.isNotBlank(globalProperties.getHubProxyPassword().orElse(null));
        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Logging level:           {}", loggingLevel);
        logger.info("Hub URL:                 {}", globalProperties.getHubUrl().orElse(""));
        logger.info("Hub Proxy Host:          {}", globalProperties.getHubProxyHost().orElse(""));
        logger.info("Hub Proxy Port:          {}", globalProperties.getHubProxyPort().orElse(""));
        logger.info("Hub Proxy Authenticated: {}", authenticatedProxy);
        logger.info("Hub Proxy User:          {}", globalProperties.getHubProxyUsername().orElse(""));

        final Optional<GlobalHubConfigEntity> optionalGlobalHubConfigEntity = globalProperties.getHubConfig();
        if (optionalGlobalHubConfigEntity.isPresent()) {
            final GlobalHubConfigEntity globalHubConfigEntity = optionalGlobalHubConfigEntity.get();
            logger.info("Hub API Token:           **********");
            logger.info("Hub Timeout:             {}", globalHubConfigEntity.getHubTimeout());
        }
        logger.info("----------------------------------------");
    }

    public void initializeChannelPropertyManagers() {
        try {
            alertStartupInitializer.initializeConfigs();
        } catch (final Exception e) {
            logger.error("Error inserting startup values", e);
        }
    }

    public void listProperties() {
        logger.info("Properties that can be used for initial Alert setup:");
        logger.info("----------------------------------------");
        for (final String property : alertStartupInitializer.getAlertPropertyNameSet()) {
            logger.info(property);
        }
        logger.info("----------------------------------------");
    }

    public void validateProviders() {
        logger.info("Validating configured providers: ");
        logger.info("----------------------------------------");
        validateHubProvider();
        logger.info("----------------------------------------");
    }

    public void validateHubProvider() {
        logger.info("Validating Hub Provider...");
        try {
            final HubServerVerifier verifier = new HubServerVerifier();
            final ProxyInfoBuilder proxyBuilder = globalProperties.createProxyInfoBuilder();
            final ProxyInfo proxyInfo = proxyBuilder.build();
            final Optional<String> hubUrlOptional = globalProperties.getHubUrl();
            if (!hubUrlOptional.isPresent()) {
                logger.error("  -> Hub Provider Invalid; cause: Hub URL missing...");
            } else {
                if (hubUrlOptional.isPresent()) {
                    final String hubUrlString = hubUrlOptional.get();
                    final Boolean trustCertificate = BooleanUtils.toBoolean(globalProperties.getHubTrustCertificate().orElse(false));

                    final URL hubUrl = new URL(hubUrlString);
                    if ("localhost".equals(hubUrl.getHost())) {
                        logger.warn("  -> Hub Provider Using localhost...");
                        final String hubWebServerEnvValue = globalProperties.getEnvironmentVariable(AlertEnvironment.PUBLIC_HUB_WEBSERVER_HOST);
                        if (StringUtils.isBlank(hubWebServerEnvValue)) {
                            logger.warn("  -> Hub Provider Using localhost because PUBLIC_HUB_WEBSERVER_HOST environment variable is not set");
                        } else {
                            logger.warn("  -> Hub Provider Using localhost because PUBLIC_HUB_WEBSERVER_HOST environment variable is set to localhost");
                        }
                    }
                    verifier.verifyIsHubServer(new URL(hubUrlString), proxyInfo, trustCertificate, globalProperties.getHubTimeout());
                    logger.info("  -> Hub Provider Valid!");
                }
            }
        } catch (final MalformedURLException | IntegrationException ex) {
            logger.error("  -> Hub Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> Hub Provider Stack Trace: ", ex);
        }
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
        scheduleTaskCrons(dailyDigestHourOfDay, purgeDataFrequencyDays);
        CompletableFuture.supplyAsync(this::purgeOldData);
    }

    public void scheduleTaskCrons(final String dailyDigestHourOfDay, final String purgeDataFrequencyDays) {
        final String dailyDigestCron = String.format("0 0 %s 1/1 * ?", dailyDigestHourOfDay);
        final String purgeDataCron = String.format("0 0 0 1/%s * ?", purgeDataFrequencyDays);
        dailyTask.scheduleExecution(dailyDigestCron);
        onDemandTask.scheduleExecutionAtFixedRate(OnDemandTask.DEFAULT_INTERVAL_SECONDS);
        purgeConfig.scheduleExecution(purgeDataCron);

        logger.info("Daily Digest next run:     {}", dailyTask.getFormatedNextRunTime().orElse(""));
        logger.info("On Demand next run:        {}", onDemandTask.getFormatedNextRunTime().orElse(""));
        logger.info("Purge Old Data next run:   {}", purgeConfig.getFormatedNextRunTime().orElse(""));

        phoneHomeTask.scheduleExecution("0 0 12 1/1 * ?");
        logger.debug("Phone home next run:       {}", phoneHomeTask.getFormatedNextRunTime());
    }

    private Boolean purgeOldData() {
        try {
            logger.info("Begin startup purge of old data");
            final List<GlobalSchedulingConfigEntity> globalSchedulingConfigs = globalSchedulingRepository.findAll();
            if (!globalSchedulingConfigs.isEmpty() && globalSchedulingConfigs.get(0) != null) {
                final GlobalSchedulingConfigEntity globalSchedulingConfig = globalSchedulingConfigs.get(0);
                final String purgeDataFrequencyDays = globalSchedulingConfig.getPurgeDataFrequencyDays();
                final PurgeReader reader = purgeConfig.createReaderWithDayOffset(Integer.valueOf(purgeDataFrequencyDays));
                final PurgeProcessor processor = purgeConfig.processor();
                final PurgeWriter writer = purgeConfig.writer();

                final List<NotificationModel> purgeData = reader.read();
                final List<NotificationModel> processedData = processor.process(purgeData);
                final List<List<NotificationModel>> dataToDelete = new ArrayList<>();
                dataToDelete.add(processedData);
                writer.write(dataToDelete);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (final Exception ex) {
            logger.error("Error occurred puring data on startup", ex);
            return Boolean.FALSE;
        } finally {
            logger.info("Finished startup purge of old data");
        }
    }

    public void startAccumulators() {
        logger.info("Starting accumulators...");
        providerDescriptorList.forEach(providerDescriptor -> {
            final Accumulator accumulator = providerDescriptor.getAccumulator();
            logger.info("  Starting accumulator: {}", accumulator.getName());
            accumulator.start();
        });
    }

}
