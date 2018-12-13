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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.scheduling.SchedulingConfiguration;
import com.synopsys.integration.alert.component.scheduling.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.SchedulingUIConfig;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.security.StringEncryptionConverter;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckProviderUIConfig;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;

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
    private final EncryptionUtility encryptionUtility;
    private final List<Descriptor> allDescriptors;
    private final BaseDescriptorAccessor descriptorAccessor;

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
    public StartupManager(final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties,
        final DailyTask dailyTask, final OnDemandTask onDemandTask, final PurgeTask purgeTask, final PhoneHomeTask phoneHometask, final AlertStartupInitializer alertStartupInitializer,
        final List<ProviderDescriptor> providerDescriptorList, final SystemStatusUtility systemStatusUtility, final SystemValidator systemValidator, final BaseConfigurationAccessor configurationAccessor,
        final EncryptionUtility encryptionUtility, final List<Descriptor> allDescriptors, final BaseDescriptorAccessor descriptorAccessor) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.dailyTask = dailyTask;
        this.onDemandTask = onDemandTask;
        this.purgeTask = purgeTask;
        phoneHomeTask = phoneHometask;
        this.alertStartupInitializer = alertStartupInitializer;
        this.providerDescriptorList = providerDescriptorList;
        this.systemStatusUtility = systemStatusUtility;
        this.systemValidator = systemValidator;
        this.configurationAccessor = configurationAccessor;
        this.encryptionUtility = encryptionUtility;
        this.allDescriptors = allDescriptors;
        this.descriptorAccessor = descriptorAccessor;
    }

    @Transactional
    public void startup() {
        logger.info("Alert Starting...");
        systemStatusUtility.startupOccurred();
        registerDescriptors();
        initializeChannelPropertyManagers();
        validate();
        logConfiguration();
        listProperties();
        initializeCronJobs();
        initializeProviders();
    }

    public void initializeChannelPropertyManagers() {
        try {
            // manually wire the encryption utility.
            StringEncryptionConverter.setEncryptionUtility(encryptionUtility);
            alertStartupInitializer.initializeConfigs(true);
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
        final Optional<ConfigurationModel> optionalGlobalBlackDuckConfigEntity = blackDuckProperties.getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfigEntity.isPresent()) {
            final ConfigurationModel globalBlackDuckConfigEntity = optionalGlobalBlackDuckConfigEntity.get();
            final FieldAccessor fieldAccessor = new FieldAccessor(globalBlackDuckConfigEntity.getCopyOfKeyToFieldMap());
            logger.info("BlackDuck API Token:           **********");
            logger.info("BlackDuck Timeout:             {}", fieldAccessor.getInteger(BlackDuckProviderUIConfig.KEY_BLACKDUCK_TIMEOUT));
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
        List<ConfigurationModel> schedulingConfigs;
        try {
            schedulingConfigs = configurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT);
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Error connecting to DB", e);
            schedulingConfigs = Collections.emptyList();
        }
        String dailyDigestHourOfDay = null;
        String purgeDataFrequencyDays = null;
        if (!schedulingConfigs.isEmpty() && schedulingConfigs.get(0) != null) {
            final ConfigurationModel globalSchedulingConfig = schedulingConfigs.get(0);
            final SchedulingConfiguration schedulingConfiguration = new SchedulingConfiguration(globalSchedulingConfig);
            dailyDigestHourOfDay = schedulingConfiguration.getDailyDigestHourOfDay();
            purgeDataFrequencyDays = schedulingConfiguration.getDataFrequencyDays();
        } else {
            dailyDigestHourOfDay = "0";
            purgeDataFrequencyDays = "3";
            final ConfigurationFieldModel hourOfDayField = ConfigurationFieldModel.create(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY);
            hourOfDayField.setFieldValue(dailyDigestHourOfDay);
            final ConfigurationFieldModel purgeFrequencyField = ConfigurationFieldModel.create(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS);
            purgeFrequencyField.setFieldValue(purgeDataFrequencyDays);
            try {
                final ConfigurationModel schedulingModel = configurationAccessor.createConfiguration(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL, Arrays.asList(hourOfDayField, purgeFrequencyField));
                logger.info("Saved scheduling to DB: {}", schedulingModel.toString());
            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Error saving to DB", e);
            }
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

    public void registerDescriptors() {
        try {
            final Set<String> registeredDescriptors = descriptorAccessor
                                                          .getRegisteredDescriptors()
                                                          .stream()
                                                          .map(RegisteredDescriptorModel::getName)
                                                          .collect(Collectors.toSet());
            final List<Descriptor> missingDescriptors = allDescriptors.stream()
                                                            .filter(descriptor -> !registeredDescriptors.contains(descriptor.getName()))
                                                            .collect(Collectors.toList());
            for (final Descriptor descriptor : missingDescriptors) {
                final String descriptorName = descriptor.getName();
                logger.info("Adding descriptor '{}'", descriptorName);
                final Collection<DefinedFieldModel> fieldModels = descriptor.createAllDefinedFields();
                final DescriptorType descriptorType = descriptor.getType();
                descriptorAccessor.registerDescriptor(descriptorName, descriptorType, fieldModels);
            }
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Error registering descriptors.");
        }
    }
}
