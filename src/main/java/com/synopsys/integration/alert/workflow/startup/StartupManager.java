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
package com.synopsys.integration.alert.workflow.startup;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.database.scheduling.SchedulingRepository;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;

@Component
public class StartupManager {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    private final SchedulingRepository schedulingRepository;
    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;
    private final DailyTask dailyTask;
    private final OnDemandTask onDemandTask;
    private final PurgeTask purgeTask;
    private final PhoneHomeTask phoneHomeTask;
    private final AlertStartupInitializer alertStartupInitializer;
    private final List<ProviderDescriptor> providerDescriptorList;
    private final SystemStatusUtility systemStatusUtility;
    private final SystemValidator systemValidator;

    @Value("${logging.level.com.blackducksoftware.integration:}")
    private String loggingLevel;

    // SSL properties
    @Value("${server.port:")
    private String serverPort;

    @Value("${server.ssl.key-store:}")
    private String keyStoreFile;

    @Value("${server.ssl.key-store-password:}")
    private String keyStorePass;

    @Value("${server.ssl.keyStoreType:}")
    private String keyStoreType;

    @Value("${server.ssl.keyAlias:}")
    private String keyAlias;

    @Value("${server.ssl.trust-store:}")
    private String trustStoreFile;

    @Value("${server.ssl.trust-store-password:}")
    private String trustStorePass;

    @Value("${server.ssl.trustStoreType:}")
    private String trustStoreType;

    @Autowired
    public StartupManager(final SchedulingRepository schedulingRepository, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties,
        final DailyTask dailyTask, final OnDemandTask onDemandTask, final PurgeTask purgeTask, final PhoneHomeTask phoneHometask, final AlertStartupInitializer alertStartupInitializer,
        final List<ProviderDescriptor> providerDescriptorList, final SystemStatusUtility systemStatusUtility, final SystemValidator systemValidator) {
        this.schedulingRepository = schedulingRepository;
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.dailyTask = dailyTask;
        this.onDemandTask = onDemandTask;
        this.purgeTask = purgeTask;
        this.phoneHomeTask = phoneHometask;
        this.alertStartupInitializer = alertStartupInitializer;
        this.providerDescriptorList = providerDescriptorList;
        this.systemStatusUtility = systemStatusUtility;
        this.systemValidator = systemValidator;
    }

    @Transactional
    public void startup() {
        logger.info("Alert Starting...");
        systemStatusUtility.startupOccurred();
        initializeChannelPropertyManagers();
        validate();
        logConfiguration();
        listProperties();
        initializeCronJobs();
        initializeProviders();
    }

    public void initializeChannelPropertyManagers() {
        try {
            alertStartupInitializer.initializeConfigs();
        } catch (final Exception e) {
            logger.error("Error inserting startup values", e);
        }
    }

    public void validate() {
        systemValidator.validate();
    }

    public void logConfiguration() {
        final boolean authenticatedProxy = StringUtils.isNotBlank(alertProperties.getAlertProxyPassword().orElse(null));
        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Logging level:           {}", getLoggingLevel());
        logger.info("Alert Proxy Host:          {}", alertProperties.getAlertProxyHost().orElse(""));
        logger.info("Alert Proxy Port:          {}", alertProperties.getAlertProxyPort().orElse(""));
        logger.info("Alert Proxy Authenticated: {}", authenticatedProxy);
        logger.info("Alert Proxy User:          {}", alertProperties.getAlertProxyUsername().orElse(""));
        logger.info("");
        logger.info("BlackDuck URL:                 {}", blackDuckProperties.getBlackDuckUrl().orElse(""));
        logger.info("BlackDuck Webserver Host:                 {}", blackDuckProperties.getPublicBlackDuckWebserverHost().orElse(""));
        logger.info("BlackDuck Webserver Port:                 {}", blackDuckProperties.getPublicBlackDuckWebserverPort().orElse(""));
        final Optional<GlobalBlackDuckConfigEntity> optionalGlobalBlackDuckConfigEntity = blackDuckProperties.getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfigEntity.isPresent()) {
            final GlobalBlackDuckConfigEntity globalBlackDuckConfigEntity = optionalGlobalBlackDuckConfigEntity.get();
            logger.info("BlackDuck API Token:           **********");
            logger.info("BlackDuck Timeout:             {}", globalBlackDuckConfigEntity.getBlackDuckTimeout());
        }
        logger.info("----------------------------------------");
    }

    public void listProperties() {
        logger.info("Properties that can be used for initial Alert setup:");
        logger.info("----------------------------------------");
        for (final String property : alertStartupInitializer.getAlertPropertyNameSet()) {
            logger.info(property);
        }
        logger.info("----------------------------------------");
    }

    @Transactional
    public void initializeCronJobs() {
        final List<SchedulingConfigEntity> globalSchedulingConfigs = schedulingRepository.findAll();
        String dailyDigestHourOfDay = null;
        String purgeDataFrequencyDays = null;
        if (!globalSchedulingConfigs.isEmpty() && globalSchedulingConfigs.get(0) != null) {
            final SchedulingConfigEntity globalSchedulingConfig = globalSchedulingConfigs.get(0);
            dailyDigestHourOfDay = globalSchedulingConfig.getDailyDigestHourOfDay();
            purgeDataFrequencyDays = globalSchedulingConfig.getPurgeDataFrequencyDays();
        } else {
            dailyDigestHourOfDay = "0";
            purgeDataFrequencyDays = "3";
            final SchedulingConfigEntity globalSchedulingConfig = new SchedulingConfigEntity(dailyDigestHourOfDay, purgeDataFrequencyDays);
            final SchedulingConfigEntity savedGlobalSchedulingConfig = schedulingRepository.save(globalSchedulingConfig);
            logger.info(savedGlobalSchedulingConfig.toString());
        }
        scheduleTaskCrons(dailyDigestHourOfDay, purgeDataFrequencyDays);
        CompletableFuture.supplyAsync(this::purgeOldData);
    }

    public void scheduleTaskCrons(final String dailyDigestHourOfDay, final String purgeDataFrequencyDays) {
        final String dailyDigestCron = String.format("0 0 %s 1/1 * ?", dailyDigestHourOfDay);
        final String purgeDataCron = String.format("0 0 0 1/%s * ?", purgeDataFrequencyDays);
        dailyTask.scheduleExecution(dailyDigestCron);
        onDemandTask.scheduleExecutionAtFixedRate(OnDemandTask.DEFAULT_INTERVAL_MILLISECONDS);
        purgeTask.scheduleExecution(purgeDataCron);

        logger.info("Daily Digest next run:     {}", dailyTask.getFormatedNextRunTime().orElse(""));
        logger.info("On Demand next run:        {}", onDemandTask.getFormatedNextRunTime().orElse(""));
        logger.info("Purge Old Data next run:   {}", purgeTask.getFormatedNextRunTime().orElse(""));

        phoneHomeTask.scheduleExecution("0 0 12 1/1 * ?");
        logger.debug("Phone home next run:       {}", phoneHomeTask.getFormatedNextRunTime());
    }

    @Transactional
    public Boolean purgeOldData() {
        try {
            logger.info("Begin startup purge of old data");
            final List<SchedulingConfigEntity> globalSchedulingConfigs = schedulingRepository.findAll();
            if (!globalSchedulingConfigs.isEmpty() && globalSchedulingConfigs.get(0) != null) {
                final SchedulingConfigEntity globalSchedulingConfig = globalSchedulingConfigs.get(0);
                final String purgeDataFrequencyDays = globalSchedulingConfig.getPurgeDataFrequencyDays();
                purgeTask.setDayOffset(Integer.valueOf(purgeDataFrequencyDays));
                purgeTask.run();
                purgeTask.resetDayOffset();
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

    public void initializeProviders() {
        logger.info("Initializing providers...");
        providerDescriptorList.stream().map(ProviderDescriptor::getProvider).forEach(Provider::initialize);
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }
}
