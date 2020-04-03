/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.workflow.scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class PhoneHomeTask extends StartupScheduledTask {
    public static final String TASK_NAME = "phonehome";
    public static final String ARTIFACT_ID = "blackduck-alert";
    public static final Long DEFAULT_TIMEOUT = 10L;
    public static final String CRON_EXPRESSION = "0 0 12 1/1 * ?";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AboutReader aboutReader;
    private final ConfigurationAccessor configurationAccessor;
    private final ProxyManager proxyManager;
    private final Gson gson;
    private final AuditUtility auditUtility;
    private List<ProviderPhoneHomeHandler> providerHandlers;

    @Value("${" + PhoneHomeClient.SKIP_PHONE_HOME_VARIABLE + ":FALSE}")
    private Boolean skipPhoneHome;

    @Autowired
    public PhoneHomeTask(TaskScheduler taskScheduler, AboutReader aboutReader, ConfigurationAccessor configurationAccessor,
        TaskManager taskManager, ProxyManager proxyManager, Gson gson, AuditUtility auditUtility, List<ProviderPhoneHomeHandler> providerHandlers) {
        super(taskScheduler, taskManager);
        this.aboutReader = aboutReader;
        this.configurationAccessor = configurationAccessor;
        this.proxyManager = proxyManager;
        this.gson = gson;
        this.auditUtility = auditUtility;
        this.providerHandlers = providerHandlers;
    }

    @Override
    public void checkTaskEnabled() {
        if (skipPhoneHome) {
            logger.info("Will not schedule the task {}. {} is TRUE. ", getTaskName(), PhoneHomeClient.SKIP_PHONE_HOME_VARIABLE);
            setEnabled(false);
        } else {
            logger.debug("Will schedule the task {}. {} is FALSE. ", getTaskName(), PhoneHomeClient.SKIP_PHONE_HOME_VARIABLE);
        }
    }

    @Override
    public void runTask() {
        String productVersion = aboutReader.getProductVersion();
        if (AboutReader.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            return;
        }

        ExecutorService phoneHomeExecutor = Executors.newSingleThreadExecutor();
        try {
            for (ProviderPhoneHomeHandler handler : providerHandlers) {
                List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(handler.getProviderKey(), ConfigContextEnum.GLOBAL);
                for (ConfigurationModel configuration : configurations) {
                    PhoneHomeRequestBody.Builder phoneHomeBuilder = new PhoneHomeRequestBody.Builder();
                    phoneHomeBuilder.setArtifactId(ARTIFACT_ID);
                    phoneHomeBuilder.setArtifactVersion(productVersion);
                    phoneHomeBuilder.setArtifactModules(getChannelMetaData().toArray(String[]::new));
                    PhoneHomeService phoneHomeService = createPhoneHomeService(phoneHomeExecutor);
                    PhoneHomeRequestBody requestBody = handler.populatePhoneHomeData(configuration, phoneHomeBuilder).build();
                    PhoneHomeResponse phoneHomeResponse = phoneHomeService.phoneHome(requestBody, System.getenv());
                    Boolean taskSucceeded = phoneHomeResponse.awaitResult(DEFAULT_TIMEOUT);
                    if (!taskSucceeded) {
                        logger.debug("Phone home task timed out and did not send any results.");
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            phoneHomeExecutor.shutdownNow();
        }
    }

    @Override
    public String scheduleCronExpression() {
        return CRON_EXPRESSION;
    }

    private PhoneHomeService createPhoneHomeService(ExecutorService phoneHomeExecutor) {
        IntLogger intLogger = new Slf4jIntLogger(logger);
        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        IntHttpClient intHttpClient = new IntHttpClient(intLogger, IntHttpClient.DEFAULT_TIMEOUT, true, proxyInfo);

        PhoneHomeClient phoneHomeClient = BlackDuckPhoneHomeHelper.createPhoneHomeClient(intLogger, intHttpClient, gson);
        return PhoneHomeService.createAsynchronousPhoneHomeService(intLogger, phoneHomeClient, phoneHomeExecutor);
    }

    private Set<String> getChannelMetaData() {
        Map<String, Integer> createdDistributions = new HashMap<>();
        String successKeyPart = "::Successes";
        List<ConfigurationJobModel> allJobs = configurationAccessor.getAllJobs();
        for (ConfigurationJobModel job : allJobs) {
            for (ConfigurationModel configuration : job.getCopyOfConfigurations()) {
                String channelName = configuration.getField(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
                String providerName = configuration.getField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");

                if (StringUtils.isBlank(channelName) || StringUtils.isBlank(providerName)) {
                    // We want to specifically get the channel configuration here and the only way to determine that is if it has these fields.
                    continue;
                }

                updateMetaDataCount(createdDistributions, channelName);
                updateMetaDataCount(createdDistributions, providerName);

                if (hasAuditSuccess(job.getJobId())) {
                    updateMetaDataCount(createdDistributions, channelName + successKeyPart);
                    updateMetaDataCount(createdDistributions, providerName + successKeyPart);
                }
            }

        }
        return createdDistributions.entrySet().stream().map(entry -> entry.getKey() + "(" + entry.getValue() + ")").collect(Collectors.toSet());
    }

    private Boolean hasAuditSuccess(UUID jobId) {
        return auditUtility.findFirstByJobId(jobId)
                   .map(AuditJobStatusModel::getStatus)
                   .stream()
                   .anyMatch(status -> AuditEntryStatus.SUCCESS.getDisplayName().equals(status));
    }

    private void updateMetaDataCount(Map<String, Integer> createdDistributions, String name) {
        Integer channelCount = createdDistributions.getOrDefault(name, 0) + 1;
        createdDistributions.put(name, channelCount);
    }

}
