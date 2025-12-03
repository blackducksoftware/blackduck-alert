/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.startup.component;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.channel.jira.cloud.task.JiraCloudSchedulingManager;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.task.JiraServerSchedulingManager;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JiraCloudJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JiraServerJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Order(65)
public class JiraTaskInitializer extends StartupComponent{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraServerSchedulingManager jiraServerSchedulingManager;
    private final JiraCloudSchedulingManager jiraCloudSchedulingManager;
    private final JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;
    private final JobAccessor jobAccessor;
    private final JiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private final JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor;
    private final BlackDuckProviderKey blackDuckProviderKey;

    @Autowired
    public JiraTaskInitializer(JiraServerSchedulingManager jiraServerSchedulingManager, JiraCloudSchedulingManager jiraCloudSchedulingManager, JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, ConfigurationFieldModelConverter configurationFieldModelConverter, JobAccessor jobAccessor, JiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor, JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor, BlackDuckProviderKey blackDuckProviderKey) {
        this.jiraServerSchedulingManager = jiraServerSchedulingManager;
        this.jiraCloudSchedulingManager = jiraCloudSchedulingManager;
        this.jiraServerGlobalConfigAccessor = jiraServerGlobalConfigAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
        this.jobAccessor = jobAccessor;
        this.jiraCloudJobDetailsAccessor = jiraCloudJobDetailsAccessor;
        this.jiraServerJobDetailsAccessor = jiraServerJobDetailsAccessor;
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    protected void initialize() {
        startJiraCloudTasks();
        startJiraServerTasks();
    }
    private void startJiraCloudTasks() {
        logger.info("Starting Jira Cloud Tasks");
        // Jira Cloud still uses FieldModels.  There is a default configuration with an empty URL.
        // Need to filter out any FieldModel that doesn't have a valid URL.
        List<ConfigurationModel> configurationModelList = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(ChannelKeys.JIRA_CLOUD);
        List<FieldModel> jiraCloudConfigurations = configurationModelList.stream()
                .map(configurationFieldModelConverter::convertToFieldModel)
                .filter(fieldModel -> StringUtils.isNotBlank(fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_URL).orElse(null)))
                .toList();

        int currentPage = 0;
        int pageSize = 100;
        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, pageSize,null,null,null,List.of(blackDuckProviderKey.getUniversalKey(), ChannelKeys.JIRA_CLOUD.getUniversalKey()));
        Set<String> projectDataForTask = new HashSet<>();
        while(currentPage < pageOfJobs.getTotalPages()) {
            List<UUID> jobIds = pageOfJobs.getModels().stream()
                    .map(DistributionJobModel::getJobId)
                    .toList();
            Set<String> projectNamesOrKeys = jobIds.stream()
                    .map(jiraCloudJobDetailsAccessor::retrieveDetails)
                    .flatMap(Optional::stream)
                    .filter(jobDetails -> StringUtils.isNotBlank(jobDetails.getIssueSummary()))
                    .map(JiraCloudJobDetailsModel::getProjectNameOrKey)
                    .collect(Collectors.toSet());
            projectDataForTask.addAll(projectNamesOrKeys);

            currentPage++;
            pageOfJobs = jobAccessor.getPageOfJobs(0, pageSize,null,null,null,List.of(blackDuckProviderKey.getUniversalKey(),ChannelKeys.JIRA_CLOUD.getUniversalKey()));
        }

        for(FieldModel fieldModel : jiraCloudConfigurations) {
            jiraCloudSchedulingManager.scheduleTasks(fieldModel,projectDataForTask);
        }
    }

    private void startJiraServerTasks() {
        logger.info("Starting Jira Server Tasks");
        long pagesOfConfiguration = jiraServerGlobalConfigAccessor.getConfigurationCount();
        int currentPage = 0;
        while ( currentPage < pagesOfConfiguration) {
            AlertPagedModel<JiraServerGlobalConfigModel> pageOfData = jiraServerGlobalConfigAccessor.getConfigurationPage(currentPage, 100,null,null,null);
            pageOfData.getModels().forEach(this::initializeJiraServerTasks);
            currentPage++;
        }
    }

    private void initializeJiraServerTasks(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        UUID jiraServerConfigId = UUID.fromString(jiraServerGlobalConfigModel.getId());
        int currentPage = 0;
        int pageSize = 100;
        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, pageSize,null,null,null,List.of(blackDuckProviderKey.getUniversalKey(), ChannelKeys.JIRA_SERVER.getUniversalKey()));
        Set<String> projectDataForTask = new HashSet<>();
        while(currentPage < pageOfJobs.getTotalPages()) {
            List<UUID> jobIds = pageOfJobs.getModels().stream()
                    .filter(jobModel -> jobModel.getChannelGlobalConfigId().equals(jiraServerConfigId))
                    .map(DistributionJobModel::getJobId)
                    .toList();
            Set<String> projectNamesOrKeys = jobIds.stream()
                    .map(jiraServerJobDetailsAccessor::retrieveDetails)
                    .flatMap(Optional::stream)
                    .filter(jobDetails -> StringUtils.isNotBlank(jobDetails.getIssueSummary()))
                    .map(JiraServerJobDetailsModel::getProjectNameOrKey)
                    .collect(Collectors.toSet());
            projectDataForTask.addAll(projectNamesOrKeys);

            currentPage++;
            pageOfJobs = jobAccessor.getPageOfJobs(0, pageSize,null,null,null,List.of(blackDuckProviderKey.getUniversalKey(), ChannelKeys.JIRA_SERVER.getUniversalKey()));
        }

            jiraServerSchedulingManager.scheduleTasks(jiraServerGlobalConfigModel ,projectDataForTask);
    }

}
