/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.job;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public final class JobFieldModelPopulationUtils {
    private static final String DEFAULT_PROVIDER_NAME = new BlackDuckProviderKey().getUniversalKey();

    public static List<JobProviderProjectFieldModel> createJobProviderProjects(DistributionJobModel jobModel) {
        return Optional.ofNullable(jobModel.getProjectFilterDetails())
            .stream()
            .flatMap(List::stream)
            .map(projectDetails -> new JobProviderProjectFieldModel(projectDetails.getName(), projectDetails.getHref(), false))
            .collect(Collectors.toList());
    }

    public static JobFieldModel createJobFieldModelWithDefaultProviderProjectState(DistributionJobModel jobModel) {
        List<JobProviderProjectFieldModel> jobProviderProjects = createJobProviderProjects(jobModel);
        return createJobFieldModel(jobModel, jobProviderProjects);
    }

    public static JobFieldModel createJobFieldModel(DistributionJobModel jobModel, List<JobProviderProjectFieldModel> jobProviderProjects) {
        FieldModel providerFieldModel = new FieldModel(DEFAULT_PROVIDER_NAME, ConfigContextEnum.DISTRIBUTION.name(), new HashMap<>());
        populateProviderFields(providerFieldModel, jobModel, jobProviderProjects);

        FieldModel channelFieldModel = new FieldModel(jobModel.getChannelDescriptorName(), ConfigContextEnum.DISTRIBUTION.name(), new HashMap<>());
        populateChannelFields(channelFieldModel, jobModel);

        String jobIdString = Optional.ofNullable(jobModel.getJobId())
            .map(UUID::toString)
            .orElse(null);
        return new JobFieldModel(jobIdString, Set.of(providerFieldModel, channelFieldModel), jobProviderProjects);
    }

    public static void populateProviderFields(FieldModel providerFieldModel, DistributionJobModel jobModel, List<JobProviderProjectFieldModel> jobProviderProjects) {
        String providerCommonConfigId = Optional.ofNullable(jobModel.getBlackDuckGlobalConfigId())
            .map(String::valueOf)
            .orElse(null);
        putField(providerFieldModel, ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, providerCommonConfigId);
        putField(providerFieldModel, ProviderDescriptor.KEY_PROCESSING_TYPE, jobModel.getProcessingType().toString());
        putField(providerFieldModel, ProviderDescriptor.KEY_NOTIFICATION_TYPES, jobModel.getNotificationTypes());

        boolean filterByProject = jobModel.isFilterByProject();
        putField(providerFieldModel, ProviderDescriptor.KEY_FILTER_BY_PROJECT, Boolean.toString(filterByProject));

        if (filterByProject) {
            jobModel.getProjectNamePattern()
                .filter(StringUtils::isNotBlank)
                .ifPresent(pattern -> putField(providerFieldModel, ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, pattern));
            jobModel.getProjectVersionNamePattern()
                .filter(StringUtils::isNotBlank)
                .ifPresent(pattern -> putField(providerFieldModel, ProviderDescriptor.KEY_PROJECT_VERSION_NAME_PATTERN, pattern));

            putJsonField(providerFieldModel, ProviderDescriptor.KEY_CONFIGURED_PROJECT, jobProviderProjects);
        }

        List<String> blackDuckPolicyNames = jobModel.getPolicyFilterPolicyNames();
        if (null == blackDuckPolicyNames || !blackDuckPolicyNames.isEmpty()) {
            putField(providerFieldModel, "blackduck.policy.notification.filter", blackDuckPolicyNames);
        }

        List<String> blackDuckJobVulnerabilitySeverityNames = jobModel.getVulnerabilityFilterSeverityNames();
        if (null == blackDuckJobVulnerabilitySeverityNames || !blackDuckJobVulnerabilitySeverityNames.isEmpty()) {
            putField(providerFieldModel, "blackduck.vulnerability.notification.filter", blackDuckJobVulnerabilitySeverityNames);
        }
    }

    public static void populateChannelFields(FieldModel channelFieldModel, DistributionJobModel jobModel) {
        String channelDescriptorName = jobModel.getChannelDescriptorName();
        putField(channelFieldModel, ChannelDescriptor.KEY_ENABLED, Boolean.toString(jobModel.isEnabled()));
        putField(channelFieldModel, ChannelDescriptor.KEY_NAME, jobModel.getName());
        putField(channelFieldModel, ChannelDescriptor.KEY_CHANNEL_NAME, channelDescriptorName);
        putField(channelFieldModel, ChannelDescriptor.KEY_PROVIDER_TYPE, DEFAULT_PROVIDER_NAME);
        putField(channelFieldModel, ChannelDescriptor.KEY_FREQUENCY, jobModel.getDistributionFrequency().name());
        putField(channelFieldModel, ProviderDescriptor.KEY_PROCESSING_TYPE, jobModel.getProcessingType().name());

        DistributionJobDetailsModel jobDetails = jobModel.getDistributionJobDetails();
        if (jobDetails.isA(ChannelKeys.AZURE_BOARDS)) {
            populateAzureBoardsFields(channelFieldModel, jobDetails.getAs(DistributionJobDetailsModel.AZURE));
        } else if (jobDetails.isA(ChannelKeys.EMAIL)) {
            populateEmailFields(channelFieldModel, jobDetails.getAs(DistributionJobDetailsModel.EMAIL));
        } else if (jobDetails.isA(ChannelKeys.JIRA_CLOUD)) {
            populateJiraCloudFields(channelFieldModel, jobDetails.getAs(DistributionJobDetailsModel.JIRA_CLOUD));
        } else if (jobDetails.isA(ChannelKeys.JIRA_SERVER)) {
            populateJiraServerFields(channelFieldModel, jobDetails.getAs(DistributionJobDetailsModel.JIRA_SERVER));
        } else if (jobDetails.isA(ChannelKeys.MS_TEAMS)) {
            populateMSTeamsField(channelFieldModel, jobDetails.getAs(DistributionJobDetailsModel.MS_TEAMS));
        } else if (jobDetails.isA(ChannelKeys.SLACK)) {
            populateSlackFields(channelFieldModel, jobDetails.getAs(DistributionJobDetailsModel.SLACK));
        }
    }

    private static void populateAzureBoardsFields(FieldModel channelFieldModel, AzureBoardsJobDetailsModel azureBoardsJobDetails) {
        if (null != azureBoardsJobDetails) {
            putField(channelFieldModel, AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT, Boolean.toString(azureBoardsJobDetails.isAddComments()));
            putField(channelFieldModel, AzureBoardsDescriptor.KEY_AZURE_PROJECT, azureBoardsJobDetails.getProjectNameOrId());
            putField(channelFieldModel, AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE, azureBoardsJobDetails.getWorkItemType());
            putField(channelFieldModel, AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE, azureBoardsJobDetails.getWorkItemCompletedState());
            putField(channelFieldModel, AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, azureBoardsJobDetails.getWorkItemReopenState());
        }
    }

    private static void populateEmailFields(FieldModel channelFieldModel, EmailJobDetailsModel emailJobDetails) {
        if (null != emailJobDetails) {
            putField(channelFieldModel, EmailDescriptor.KEY_SUBJECT_LINE, emailJobDetails.getSubjectLine().orElse(null));
            putField(channelFieldModel, EmailDescriptor.KEY_EMAIL_ATTACHMENT_FORMAT, emailJobDetails.getAttachmentFileType());
            putField(channelFieldModel, EmailDescriptor.KEY_PROJECT_OWNER_ONLY, Boolean.toString(emailJobDetails.isProjectOwnerOnly()));
            putField(channelFieldModel, EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, Boolean.toString(emailJobDetails.isAdditionalEmailAddressesOnly()));

            List<String> emailAddresses = emailJobDetails.getAdditionalEmailAddresses();
            if (!emailAddresses.isEmpty()) {
                putField(channelFieldModel, EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, emailAddresses);
            }
        }
    }

    private static void populateJiraCloudFields(FieldModel channelFieldModel, JiraCloudJobDetailsModel jiraCloudJobDetails) {
        if (null != jiraCloudJobDetails) {
            putField(channelFieldModel, JiraCloudDescriptor.KEY_ADD_COMMENTS, Boolean.toString(jiraCloudJobDetails.isAddComments()));
            putField(channelFieldModel, JiraCloudDescriptor.KEY_ISSUE_CREATOR, jiraCloudJobDetails.getIssueCreatorEmail());
            putField(channelFieldModel, JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME, jiraCloudJobDetails.getProjectNameOrKey());
            putField(channelFieldModel, JiraCloudDescriptor.KEY_ISSUE_TYPE, jiraCloudJobDetails.getIssueType());
            putField(channelFieldModel, JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, jiraCloudJobDetails.getResolveTransition());
            putField(channelFieldModel, JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, jiraCloudJobDetails.getReopenTransition());
            putJsonField(channelFieldModel, JiraCloudDescriptor.KEY_FIELD_MAPPING, jiraCloudJobDetails.getCustomFields());
            putField(channelFieldModel, JiraCloudDescriptor.KEY_ISSUE_SUMMARY, jiraCloudJobDetails.getIssueSummary());
        }
    }

    private static void populateJiraServerFields(FieldModel channelFieldModel, JiraServerJobDetailsModel jiraServerJobDetails) {
        if (null != jiraServerJobDetails) {
            putField(channelFieldModel, JiraServerDescriptor.KEY_ADD_COMMENTS, Boolean.toString(jiraServerJobDetails.isAddComments()));
            putField(channelFieldModel, JiraServerDescriptor.KEY_ISSUE_CREATOR, jiraServerJobDetails.getIssueCreatorUsername());
            putField(channelFieldModel, JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, jiraServerJobDetails.getProjectNameOrKey());
            putField(channelFieldModel, JiraServerDescriptor.KEY_ISSUE_TYPE, jiraServerJobDetails.getIssueType());
            putField(channelFieldModel, JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, jiraServerJobDetails.getResolveTransition());
            putField(channelFieldModel, JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, jiraServerJobDetails.getReopenTransition());
            putJsonField(channelFieldModel, JiraServerDescriptor.KEY_FIELD_MAPPING, jiraServerJobDetails.getCustomFields());
            putField(channelFieldModel, JiraServerDescriptor.KEY_ISSUE_SUMMARY, jiraServerJobDetails.getIssueSummary());
        }
    }

    private static void populateMSTeamsField(FieldModel channelFieldModel, MSTeamsJobDetailsModel msTeamsJobDetails) {
        if (null != msTeamsJobDetails) {
            putField(channelFieldModel, MsTeamsDescriptor.KEY_WEBHOOK, msTeamsJobDetails.getWebhook());
        }
    }

    private static void populateSlackFields(FieldModel channelFieldModel, SlackJobDetailsModel slackJobDetails) {
        if (null != slackJobDetails) {
            putField(channelFieldModel, SlackDescriptor.KEY_WEBHOOK, slackJobDetails.getWebhook());
            putField(channelFieldModel, SlackDescriptor.KEY_CHANNEL_NAME, slackJobDetails.getChannelName());
            putField(channelFieldModel, SlackDescriptor.KEY_CHANNEL_USERNAME, slackJobDetails.getChannelUsername());
        }
    }

    private static void putField(FieldModel fieldModel, String key, String value) {
        if (null != value) {
            putField(fieldModel, key, List.of(value));
        }
    }

    private static void putField(FieldModel fieldModel, String key, List<String> values) {
        FieldValueModel fieldValueModel = new FieldValueModel(values, true);
        fieldModel.putField(key, fieldValueModel);
    }

    private static <T extends AlertSerializableModel> void putJsonField(FieldModel fieldModel, String fieldName, List<T> fields) {
        // Convert to JSON for 6.4.0 while the dynamic ui still uses these as initial values on edit/copy
        List<String> fieldJson = fields
            .stream()
            .map(AlertSerializableModel::toString)
            .collect(Collectors.toList());
        if (!fieldJson.isEmpty()) {
            putField(fieldModel, fieldName, fieldJson);
        }
    }

}
