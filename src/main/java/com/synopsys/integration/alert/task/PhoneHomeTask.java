/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.task;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.alert.api.task.StartupScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.RestApiAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.alert.web.api.about.AboutReader;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeClient;
import com.synopsys.integration.phonehome.PhoneHomeResponse;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.phonehome.google.analytics.GoogleAnalyticsConstants;
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
    public static String SUCCESS_KEY_PART = "::Successes";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AboutReader aboutReader;
    private final JobAccessor jobAccessor;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ProxyManager proxyManager;
    private final Gson gson;
    private final RestApiAuditAccessor auditAccessor;
    private final List<ProviderPhoneHomeHandler> providerHandlers;
    private final ProviderKey providerKey;

    @Value("${" + PhoneHomeClient.SKIP_PHONE_HOME_VARIABLE + ":FALSE}")
    private Boolean skipPhoneHome;

    @Autowired
    public PhoneHomeTask(
        TaskScheduler taskScheduler,
        AboutReader aboutReader,
        JobAccessor jobAccessor,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        TaskManager taskManager,
        ProxyManager proxyManager,
        Gson gson,
        RestApiAuditAccessor auditAccessor,
        List<ProviderPhoneHomeHandler> providerHandlers,
        ProviderKey providerKey) {
        super(taskScheduler, taskManager);
        this.aboutReader = aboutReader;
        this.jobAccessor = jobAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.proxyManager = proxyManager;
        this.gson = gson;
        this.auditAccessor = auditAccessor;
        this.providerHandlers = providerHandlers;
        this.providerKey = providerKey;
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
                List<ConfigurationModel> configurations = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(handler.getProviderKey(), ConfigContextEnum.GLOBAL);
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
        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(GoogleAnalyticsConstants.BASE_URL);
        IntHttpClient intHttpClient = new IntHttpClient(intLogger, gson, IntHttpClient.DEFAULT_TIMEOUT, true, proxyInfo);

        PhoneHomeClient phoneHomeClient = BlackDuckPhoneHomeHelper.createPhoneHomeClient(intLogger, intHttpClient.getClientBuilder(), gson);
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
        for (DistributionJobModel job : jobs) {
            String channelName = job.getChannelDescriptorName();
            String providerName = providerKey.getUniversalKey();

            updateMetaDataCount(createdDistributions, channelName);
            updateMetaDataCount(createdDistributions, providerName);

            if (hasAuditSuccess(job.getJobId())) {
                updateMetaDataCount(createdDistributions, channelName + SUCCESS_KEY_PART);
                updateMetaDataCount(createdDistributions, providerName + SUCCESS_KEY_PART);
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
