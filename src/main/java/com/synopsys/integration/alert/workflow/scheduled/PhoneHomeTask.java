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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.web.api.about.AboutReader;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.util.NameVersion;

@Component
public class PhoneHomeTask extends StartupScheduledTask {
    public static final String TASK_NAME = "phonehome";
    public static final String ARTIFACT_ID = "blackduck-alert";
    public static final Long DEFAULT_TIMEOUT = 10L;
    public static final String CRON_EXPRESSION = "0 0 12 1/1 * ?";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AboutReader aboutReader;
    private final JobAccessorV2 jobAccessor;
    private final ConfigurationAccessor configurationAccessor;
    private final ProxyManager proxyManager;
    private final Gson gson;
    private final AuditAccessor auditAccessor;
    private final List<ProviderPhoneHomeHandler> providerHandlers;

    @Value("${" + PhoneHomeClient.SKIP_PHONE_HOME_VARIABLE + ":FALSE}")
    private Boolean skipPhoneHome;

    @Autowired
    public PhoneHomeTask(
        TaskScheduler taskScheduler,
        AboutReader aboutReader,
        JobAccessorV2 jobAccessor,
        ConfigurationAccessor configurationAccessor,
        TaskManager taskManager,
        ProxyManager proxyManager,
        Gson gson,
        AuditAccessor auditAccessor,
        List<ProviderPhoneHomeHandler> providerHandlers
    ) {
        super(taskScheduler, taskManager);
        this.aboutReader = aboutReader;
        this.jobAccessor = jobAccessor;
        this.configurationAccessor = configurationAccessor;
        this.proxyManager = proxyManager;
        this.gson = gson;
        this.auditAccessor = auditAccessor;
        this.providerHandlers = providerHandlers;
    }

    @Override
    public void checkTaskEnabled() {
        if (getSkipPhoneHome()) {
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
            String[] channelMetaData = retrieveChannelMetadataForAllJobs();

            for (ProviderPhoneHomeHandler handler : providerHandlers) {
                List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(handler.getProviderKey(), ConfigContextEnum.GLOBAL);
                for (ConfigurationModel configuration : configurations) {
                    PhoneHomeService phoneHomeService = createPhoneHomeService(phoneHomeExecutor);
                    NameVersion alertArtifactInfo = new NameVersion(ARTIFACT_ID, productVersion);
                    PhoneHomeRequestBodyBuilder requestBodyBuilder = handler.populatePhoneHomeData(configuration, alertArtifactInfo);
                    requestBodyBuilder.addArtifactModules(channelMetaData);
                    PhoneHomeRequestBody requestBody = requestBodyBuilder.build();
                    PhoneHomeResponse phoneHomeResponse = phoneHomeService.phoneHome(requestBody, System.getenv());
                    boolean taskSucceeded = BooleanUtils.isTrue(phoneHomeResponse.awaitResult(DEFAULT_TIMEOUT));
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

    private String[] retrieveChannelMetadataForAllJobs() {
        Set<String> channelMetadata = new HashSet<>();

        int pageNumber = 0;
        AlertPagedModel<DistributionJobModel> pageOfJobs;
        do {
            pageOfJobs = jobAccessor.getPageOfJobs(pageNumber, 100);
            Set<String> channelMetadataBatch = retrieveChannelMetadataForJobs(pageOfJobs.getModels());
            channelMetadata.addAll(channelMetadataBatch);
            pageNumber++;
        } while (pageNumber < pageOfJobs.getTotalPages());
        return channelMetadata.toArray(String[]::new);
    }

    private Set<String> retrieveChannelMetadataForJobs(List<DistributionJobModel> jobs) {
        Map<String, Integer> createdDistributions = new HashMap<>();
        String successKeyPart = "::Successes";
        for (DistributionJobModel job : jobs) {
            String channelName = job.getChannelDescriptorName();
            String providerName = new BlackDuckProviderKey().getUniversalKey();

            if (StringUtils.isBlank(channelName)) {
                // We want to specifically get the channel configuration here and the only way to determine that is if it has this field.
                continue;
            }

            updateMetaDataCount(createdDistributions, channelName);
            updateMetaDataCount(createdDistributions, providerName);

            if (hasAuditSuccess(job.getJobId())) {
                updateMetaDataCount(createdDistributions, channelName + successKeyPart);
                updateMetaDataCount(createdDistributions, providerName + successKeyPart);
            }

        }
        return createdDistributions.entrySet().stream().map(entry -> entry.getKey() + "(" + entry.getValue() + ")").collect(Collectors.toSet());
    }

    private boolean hasAuditSuccess(UUID jobId) {
        return auditAccessor.findFirstByJobId(jobId)
                   .map(AuditJobStatusModel::getStatus)
                   .stream()
                   .anyMatch(status -> AuditEntryStatus.SUCCESS.getDisplayName().equals(status));
    }

    private void updateMetaDataCount(Map<String, Integer> createdDistributions, String name) {
        Integer channelCount = createdDistributions.getOrDefault(name, 0) + 1;
        createdDistributions.put(name, channelCount);
    }

    public boolean getSkipPhoneHome() {
        return BooleanUtils.isTrue(skipPhoneHome);
    }

}
