/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.ProxyManager;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertUpgradeException;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.workflow.TaskManager;
import com.synopsys.integration.alert.component.scheduling.SchedulingConfiguration;
import com.synopsys.integration.alert.component.scheduling.SchedulingDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;
import com.synopsys.integration.alert.workflow.upgrade.UpgradeProcessor;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class StartupManager {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

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
    private final BaseConfigurationAccessor configurationAccessor;
    private final UpgradeProcessor upgradeProcessor;
    private final ProxyManager proxyManager;
    private final TaskManager taskManager;

    @Autowired
    public StartupManager(final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties,
        final DailyTask dailyTask, final OnDemandTask onDemandTask, final PurgeTask purgeTask, final PhoneHomeTask phoneHomeTask, final AlertStartupInitializer alertStartupInitializer,
        final List<ProviderDescriptor> providerDescriptorList, final SystemStatusUtility systemStatusUtility, final SystemValidator systemValidator, final BaseConfigurationAccessor configurationAccessor,
        final UpgradeProcessor upgradeProcessor, final ProxyManager proxyManager, final TaskManager taskManager) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.dailyTask = dailyTask;
        this.onDemandTask = onDemandTask;
        this.purgeTask = purgeTask;
        this.phoneHomeTask = phoneHomeTask;
        this.alertStartupInitializer = alertStartupInitializer;
        this.providerDescriptorList = providerDescriptorList;
        this.systemStatusUtility = systemStatusUtility;
        this.systemValidator = systemValidator;
        this.configurationAccessor = configurationAccessor;
        this.upgradeProcessor = upgradeProcessor;
        this.proxyManager = proxyManager;
        this.taskManager = taskManager;
    }

    @Transactional
    public void startup() throws AlertUpgradeException {
        logger.info("Alert Starting...");
        // FIXME move any descriptor logic to the build.gradle
        systemStatusUtility.startupOccurred();
        if (upgradeProcessor.shouldUpgrade()) {
            upgradeProcessor.runUpgrade();
        }
        initializeChannelPropertyManagers();
        validate();
        logConfiguration();
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
        final ProxyInfo proxyInfo = proxyManager.createProxyInfo();

        final boolean authenticatedProxy = StringUtils.isNotBlank(proxyInfo.getPassword().orElse(""));
        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Logging level:           {}", alertProperties.getLoggingLevel().orElse(""));
        logger.info("Alert Proxy Host:          {}", proxyInfo.getHost().orElse(""));
        logger.info("Alert Proxy Port:          {}", proxyInfo.getPort());
        logger.info("Alert Proxy Authenticated: {}", authenticatedProxy);
        logger.info("Alert Proxy User:          {}", proxyInfo.getUsername().orElse(""));
        logger.info("");
        logger.info("BlackDuck URL:                 {}", blackDuckProperties.getBlackDuckUrl().orElse(""));
        logger.info("BlackDuck Webserver Host:                 {}", blackDuckProperties.getPublicBlackDuckWebserverHost().orElse(""));
        logger.info("BlackDuck Webserver Port:                 {}", blackDuckProperties.getPublicBlackDuckWebserverPort().orElse(""));
        final Optional<ConfigurationModel> optionalGlobalBlackDuckConfigEntity = blackDuckProperties.getBlackDuckConfig();
        optionalGlobalBlackDuckConfigEntity.ifPresent(configurationModel -> {
            final FieldAccessor fieldAccessor = new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap());
            logger.info("BlackDuck API Token:           **********");
            logger.info("BlackDuck Timeout:             {}", fieldAccessor.getInteger(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT).orElse(BlackDuckProperties.DEFAULT_TIMEOUT));
        });
        logger.info("----------------------------------------");
    }

    @Transactional
    public void initializeCronJobs() {
        List<ConfigurationModel> schedulingConfigs;
        try {
            schedulingConfigs = configurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT);
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Error connecting to DB", e);
            schedulingConfigs = Collections.emptyList();
        }
        final String defaultDailyHourOfDay = "0";
        final String defaultPurgeFrequencyInDays = "3";
        String dailyHourOfDay = defaultDailyHourOfDay;
        String purgeDataFrequencyDays = defaultPurgeFrequencyInDays;

        final ConfigurationFieldModel defaultHourOfDayField = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
        defaultHourOfDayField.setFieldValue(dailyHourOfDay);
        final ConfigurationFieldModel defaultPurgeFrequencyField = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS);
        defaultPurgeFrequencyField.setFieldValue(purgeDataFrequencyDays);

        final Optional<ConfigurationModel> schedulingConfig = schedulingConfigs.stream().findFirst();
        if (schedulingConfig.isPresent()) {
            final ConfigurationModel globalSchedulingConfig = schedulingConfig.get();
            final List<ConfigurationFieldModel> fields = new ArrayList<>(2);
            final Optional<ConfigurationFieldModel> configuredDailyHour = globalSchedulingConfig.getField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
            final Optional<ConfigurationFieldModel> configuredPurgeFrequency = globalSchedulingConfig.getField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS);
            final boolean updateConfiguration = configuredDailyHour.isEmpty() || configuredPurgeFrequency.isEmpty();
            if (updateConfiguration) {
                configuredDailyHour.ifPresentOrElse(fields::add, () -> fields.add(defaultHourOfDayField));
                configuredPurgeFrequency.ifPresentOrElse(fields::add, () -> fields.add(defaultPurgeFrequencyField));
                try {
                    final ConfigurationModel schedulingModel = configurationAccessor.updateConfiguration(globalSchedulingConfig.getConfigurationId(), fields);
                    logger.info("Saved updated scheduling to DB: {}", schedulingModel.toString());
                } catch (final AlertDatabaseConstraintException e) {
                    logger.error("Error saving to DB", e);
                }
            }

            if (configuredDailyHour.isPresent()) {
                dailyHourOfDay = configuredDailyHour.get().getFieldValue().orElse(defaultDailyHourOfDay);
            }

            if (configuredPurgeFrequency.isPresent()) {
                purgeDataFrequencyDays = configuredPurgeFrequency.get().getFieldValue().orElse(defaultPurgeFrequencyInDays);
            }
        } else {
            try {
                final ConfigurationModel schedulingModel = configurationAccessor.createConfiguration(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL, List.of(defaultHourOfDayField, defaultPurgeFrequencyField));
                logger.info("Saved scheduling to DB: {}", schedulingModel.toString());
            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Error saving to DB", e);
            }
        }
        scheduleTaskCrons(dailyHourOfDay, purgeDataFrequencyDays);
        CompletableFuture.supplyAsync(this::purgeOldData);
    }

    public void scheduleTaskCrons(final String dailyDigestHourOfDay, final String purgeDataFrequencyDays) {
        final String dailyDigestCron = String.format(DailyTask.CRON_FORMAT, dailyDigestHourOfDay);
        final String purgeDataCron = String.format(PurgeTask.CRON_FORMAT, purgeDataFrequencyDays);
        taskManager.registerTask(dailyTask);
        taskManager.registerTask(purgeTask);
        taskManager.registerTask(onDemandTask);
        taskManager.registerTask(phoneHomeTask);
        taskManager.scheduleCronTask(dailyDigestCron, dailyTask.getTaskName());
        taskManager.scheduleCronTask(purgeDataCron, purgeTask.getTaskName());
        taskManager.scheduleCronTask(PhoneHomeTask.CRON_EXPRESSION, phoneHomeTask.getTaskName());
        taskManager.scheduleExecutionAtFixedRate(OnDemandTask.DEFAULT_INTERVAL_MILLISECONDS, onDemandTask.getTaskName());

        logger.info("Daily Digest next run:     {}", taskManager.getNextRunTime(dailyTask.getTaskName()));
        logger.info("On Demand next run:        {}", taskManager.getNextRunTime(onDemandTask.getTaskName()));
        logger.info("Purge Old Data next run:   {}", taskManager.getNextRunTime(purgeTask.getTaskName()));
        logger.debug("Phone home next run:       {}", taskManager.getNextRunTime(phoneHomeTask.getTaskName()));
    }

    @Transactional
    public Boolean purgeOldData() {
        try {
            logger.info("Begin startup purge of old data");
            final List<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationByDescriptorNameAndContext(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL);
            if (!configurationModel.isEmpty() && configurationModel.get(0) != null) {
                final ConfigurationModel globalSchedulingConfig = configurationModel.get(0);
                final SchedulingConfiguration schedulingConfiguration = new SchedulingConfiguration(globalSchedulingConfig);
                final String purgeDataFrequencyDays = schedulingConfiguration.getDataFrequencyDays();
                purgeTask.setDayOffset(Integer.valueOf(purgeDataFrequencyDays));
                purgeTask.run();
                purgeTask.resetDayOffset();
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (final Exception ex) {
            logger.error("Error occurred purging data on startup", ex);
            return Boolean.FALSE;
        } finally {
            logger.info("Finished startup purge of old data");
        }
    }

    public void initializeProviders() {
        logger.info("Initializing providers...");
        providerDescriptorList.stream().map(ProviderDescriptor::getProvider).forEach(Provider::initialize);
    }
}
