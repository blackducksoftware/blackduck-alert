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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.purge.PurgeProcessor;
import com.synopsys.integration.alert.database.purge.PurgeReader;
import com.synopsys.integration.alert.database.purge.PurgeWriter;
import com.synopsys.integration.alert.database.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.database.scheduling.SchedulingRepository;
import com.synopsys.integration.alert.database.security.StringEncryptionConverter;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;
import com.synopsys.integration.blackduck.service.model.HubServerVerifier;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

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

    private final StringEncryptionConverter stringEncryptionConverter;
    private final SystemStatusUtility systemStatusUtility;

    @Autowired
    public StartupManager(final SchedulingRepository schedulingRepository, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties,
        final DailyTask dailyTask, final OnDemandTask onDemandTask, final PurgeTask purgeTask, final PhoneHomeTask phoneHometask, final AlertStartupInitializer alertStartupInitializer,
        final List<ProviderDescriptor> providerDescriptorList, final StringEncryptionConverter stringEncryptionConverter, final SystemStatusUtility systemStatusUtility) {
        this.schedulingRepository = schedulingRepository;
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.dailyTask = dailyTask;
        this.onDemandTask = onDemandTask;
        this.purgeTask = purgeTask;
        this.phoneHomeTask = phoneHometask;
        this.alertStartupInitializer = alertStartupInitializer;
        this.providerDescriptorList = providerDescriptorList;
        this.stringEncryptionConverter = stringEncryptionConverter;
        this.systemStatusUtility = systemStatusUtility;
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
        checkEncryptionProperties();
        validateProviders();
    }

    public void checkEncryptionProperties() {
        if (stringEncryptionConverter.isInitialized()) {
            logger.info("Encryption utilities: Initialized");
        } else {
            logger.error("Encryption utilities: Not Initialized");
            systemStatusUtility.addSystemMessage("Encryption utility not initialized", SystemMessageType.ERROR);
        }
    }

    public void validateProviders() {
        logger.info("Validating configured providers: ");
        logger.info("----------------------------------------");
        validateBlackDuckProvider();
        logger.info("----------------------------------------");
    }

    // TODO add this validation to provider descriptors so we can run this when it's defined
    public void validateBlackDuckProvider() {
        logger.info("Validating Black Duck Provider...");
        try {
            final HubServerVerifier verifier = new HubServerVerifier();
            final ProxyInfoBuilder proxyBuilder = createProxyInfoBuilder();
            final ProxyInfo proxyInfo = proxyBuilder.build();
            final Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (!blackDuckUrlOptional.isPresent()) {
                logger.error("  -> Black Duck Provider Invalid; cause: Black Duck URL missing...");
                systemStatusUtility.addSystemMessage("Black Duck Provider invalid: URL missing", SystemMessageType.ERROR);
            } else {
                final String blackDuckUrlString = blackDuckUrlOptional.get();
                final Boolean trustCertificate = BooleanUtils.toBoolean(alertProperties.getAlertTrustCertificate().orElse(false));
                final Integer timeout = blackDuckProperties.getBlackDuckTimeout();
                logger.debug("  -> Black Duck Provider URL found validating: {}", blackDuckUrlString);
                logger.debug("  -> Black Duck Provider Trust Cert: {}", trustCertificate);
                logger.debug("  -> Black Duck Provider Timeout: {}", timeout);
                final URL blackDuckUrl = new URL(blackDuckUrlString);
                if ("localhost".equals(blackDuckUrl.getHost())) {
                    logger.warn("  -> Black Duck Provider Using localhost...");
                    final String blackDuckWebServerHost = blackDuckProperties.getPublicBlackDuckWebserverHost().orElse("");
                    logger.warn("  -> Black Duck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to {}", blackDuckWebServerHost);
                    systemStatusUtility.addSystemMessage("BlackDuck Provider Using localhost", SystemMessageType.WARNING);
                }
                verifier.verifyIsHubServer(blackDuckUrl, proxyInfo, trustCertificate, timeout);
                logger.info("  -> Black Duck Provider Valid!");
            }
        } catch (final MalformedURLException | IntegrationException ex) {
            logger.error("  -> Black Duck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> Black Duck Provider Stack Trace: ", ex);
            systemStatusUtility.addSystemMessage("Black Duck Provider invalid: " + ex.getMessage(), SystemMessageType.ERROR);
        }
    }

    private ProxyInfoBuilder createProxyInfoBuilder() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        final Optional<String> alertProxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> alertProxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> alertProxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> alertProxyPassword = alertProperties.getAlertProxyPassword();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent()) {
            proxyBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            proxyBuilder.setPassword(alertProxyPassword.get());
        }
        return proxyBuilder;
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
        logger.info("Black Duck URL:                 {}", blackDuckProperties.getBlackDuckUrl().orElse(""));
        logger.info("Black Duck Webserver Host:                 {}", blackDuckProperties.getPublicBlackDuckWebserverHost().orElse(""));
        logger.info("Black Duck Webserver Port:                 {}", blackDuckProperties.getPublicBlackDuckWebserverPort().orElse(""));
        final Optional<GlobalBlackDuckConfigEntity> optionalGlobalBlackDuckConfigEntity = blackDuckProperties.getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfigEntity.isPresent()) {
            final GlobalBlackDuckConfigEntity globalBlackDuckConfigEntity = optionalGlobalBlackDuckConfigEntity.get();
            logger.info("Black Duck API Token:           **********");
            logger.info("Black Duck Timeout:             {}", globalBlackDuckConfigEntity.getBlackDuckTimeout());
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
                final PurgeReader reader = purgeTask.createReaderWithDayOffset(Integer.valueOf(purgeDataFrequencyDays));
                final PurgeProcessor processor = purgeTask.processor();
                final PurgeWriter writer = purgeTask.writer();

                final List<NotificationContent> purgeData = reader.read();
                final List<NotificationContent> processedData = processor.process(purgeData);
                final List<List<NotificationContent>> dataToDelete = new ArrayList<>();
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

    public void initializeProviders() {
        logger.info("Initializing providers...");
        providerDescriptorList.stream().map(ProviderDescriptor::getProvider).forEach(Provider::initialize);
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }
}
