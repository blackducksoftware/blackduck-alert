package com.synopsys.integration.alert.database.job;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeEntity;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectEntity;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterEntity;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;

public class JobConfigurationModelFieldPopulationUtils {
    public static void populateBlackDuckConfigurationModelFields(DistributionJobEntity jobEntity, ConfigurationModelMutable blackDuckConfigurationModel) {
        BlackDuckJobDetailsEntity blackDuckJobDetails = jobEntity.getBlackDuckJobDetails();

        blackDuckConfigurationModel.put(createConfigFieldModel("provider.common.config.id", blackDuckJobDetails.getGlobalConfigId().toString()));
        blackDuckConfigurationModel.put(createConfigFieldModel("provider.distribution.processing.type", jobEntity.getProcessingType()));

        Boolean filterByProject = blackDuckJobDetails.getFilterByProject();
        blackDuckConfigurationModel.put(createConfigFieldModel("channel.common.filter.by.project", filterByProject.toString()));
        if (filterByProject) {
            String projectNamePattern = blackDuckJobDetails.getProjectNamePattern();
            if (StringUtils.isNotBlank(projectNamePattern)) {
                blackDuckConfigurationModel.put(createConfigFieldModel("channel.common.project.name.pattern", projectNamePattern));
            }

            List<BlackDuckJobProjectEntity> blackDuckJobProjects = blackDuckJobDetails.getBlackDuckJobProjects();
            if (null != blackDuckJobProjects && !blackDuckJobProjects.isEmpty()) {
                List<String> blackDuckProjectNames = blackDuckJobProjects
                                                         .stream()
                                                         .map(BlackDuckJobProjectEntity::getProjectName)
                                                         .collect(Collectors.toList());
                blackDuckConfigurationModel.put(createConfigFieldModel("channel.common.configured.project", blackDuckProjectNames));
            }
        }

        // These are required so they will not be null/empty
        List<String> blackDuckJobNotificationTypeNames = blackDuckJobDetails.getBlackDuckJobNotificationTypes()
                                                             .stream()
                                                             .map(BlackDuckJobNotificationTypeEntity::getNotificationType)
                                                             .collect(Collectors.toList());
        blackDuckConfigurationModel.put(createConfigFieldModel("provider.distribution.notification.types", blackDuckJobNotificationTypeNames));

        List<BlackDuckJobPolicyFilterEntity> blackDuckJobPolicyFilters = blackDuckJobDetails.getBlackDuckJobPolicyFilters();
        if (null != blackDuckJobPolicyFilters && !blackDuckJobPolicyFilters.isEmpty()) {
            List<String> blackDuckPolicyNames = blackDuckJobPolicyFilters
                                                    .stream()
                                                    .map(BlackDuckJobPolicyFilterEntity::getPolicyName)
                                                    .collect(Collectors.toList());
            blackDuckConfigurationModel.put(createConfigFieldModel("blackduck.policy.notification.filter", blackDuckPolicyNames));
        }

        List<BlackDuckJobVulnerabilitySeverityFilterEntity> blackDuckJobVulnerabilitySeverityFilters = blackDuckJobDetails.getBlackDuckJobVulnerabilitySeverityFilters();
        if (null != blackDuckJobVulnerabilitySeverityFilters && !blackDuckJobVulnerabilitySeverityFilters.isEmpty()) {
            List<String> blackDuckJobVulnerabilitySeverityNames = blackDuckJobVulnerabilitySeverityFilters
                                                                      .stream()
                                                                      .map(BlackDuckJobVulnerabilitySeverityFilterEntity::getSeverityName)
                                                                      .collect(Collectors.toList());
            blackDuckConfigurationModel.put(createConfigFieldModel("blackduck.vulnerability.notification.filter", blackDuckJobVulnerabilitySeverityNames));
        }
    }

    public static void populateChannelConfigurationModelFields(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        String channelDescriptorName = jobEntity.getChannelDescriptorName();
        channelConfigurationModel.put(createConfigFieldModel("channel.common.enabled", jobEntity.getEnabled().toString()));
        channelConfigurationModel.put(createConfigFieldModel("channel.common.name", jobEntity.getName()));
        channelConfigurationModel.put(createConfigFieldModel("channel.common.channel.name", channelDescriptorName));
        channelConfigurationModel.put(createConfigFieldModel("channel.common.frequency", jobEntity.getDistributionFrequency()));
        channelConfigurationModel.put(createConfigFieldModel("provider.distribution.processing.type", jobEntity.getProcessingType()));

        if ("channel_azure_boards".equals(channelDescriptorName)) {
            populateAzureBoardsFields(jobEntity, channelConfigurationModel);
        } else if ("channel_email".equals(channelDescriptorName)) {
            populateEmailFields(jobEntity, channelConfigurationModel);
        } else if ("channel_jira_cloud".equals(channelDescriptorName)) {
            populateJiraCloudFields(jobEntity, channelConfigurationModel);
        } else if ("channel_jira_server".equals(channelDescriptorName)) {
            populateJiraServerFields(jobEntity, channelConfigurationModel);
        } else if ("msteamskey".equals(channelDescriptorName)) {
            populateMSTeamsField(jobEntity, channelConfigurationModel);
        } else if ("channel_slack".equals(channelDescriptorName)) {
            populateSlackFields(jobEntity, channelConfigurationModel);
        }
    }

    public static ConfigurationFieldModel createConfigFieldModel(String fieldKey, String value) {
        return createConfigFieldModel(fieldKey, List.of(value));
    }

    public static ConfigurationFieldModel createConfigFieldModel(String fieldKey, Collection<String> values) {
        ConfigurationFieldModel fieldModel = ConfigurationFieldModel.create(fieldKey);
        fieldModel.setFieldValues(values);
        return fieldModel;
    }

    private static void populateAzureBoardsFields(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        AzureBoardsJobDetailsEntity azureBoardsJobDetails = jobEntity.getAzureBoardsJobDetails();
        if (null != azureBoardsJobDetails) {
            channelConfigurationModel.put(createConfigFieldModel("channel.azure.boards.work.item.comment", azureBoardsJobDetails.getAddComments().toString()));
            channelConfigurationModel.put(createConfigFieldModel("channel.azure.boards.project", azureBoardsJobDetails.getProjectNameOrId()));
            channelConfigurationModel.put(createConfigFieldModel("channel.azure.boards.work.item.type", azureBoardsJobDetails.getWorkItemType()));
            channelConfigurationModel.put(createConfigFieldModel("channel.azure.boards.work.item.completed.state", azureBoardsJobDetails.getWorkItemCompletedState()));
            channelConfigurationModel.put(createConfigFieldModel("channel.azure.boards.work.item.reopen.state", azureBoardsJobDetails.getWorkItemReopenState()));
        }
    }

    private static void populateEmailFields(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        EmailJobDetailsEntity emailJobDetails = jobEntity.getEmailJobDetails();
        if (null != emailJobDetails) {
            channelConfigurationModel.put(createConfigFieldModel("email.subject.line", emailJobDetails.getSubjectLine()));
            channelConfigurationModel.put(createConfigFieldModel("email.attachment.format", emailJobDetails.getAttachmentFileType()));
            channelConfigurationModel.put(createConfigFieldModel("project.owner.only", emailJobDetails.getProjectOwnerOnly().toString()));
            channelConfigurationModel.put(createConfigFieldModel("email.additional.addresses.only", emailJobDetails.getAdditionalEmailAddressesOnly().toString()));

            List<EmailJobAdditionalEmailAddressEntity> emailJobAdditionalEmailAddresses = emailJobDetails.getEmailJobAdditionalEmailAddresses();
            if (null != emailJobAdditionalEmailAddresses) {
                List<String> emailAddresses = emailJobAdditionalEmailAddresses
                                                  .stream()
                                                  .map(EmailJobAdditionalEmailAddressEntity::getEmailAddress)
                                                  .collect(Collectors.toList());
                channelConfigurationModel.put(createConfigFieldModel("email.additional.addresses", emailAddresses));
            }
        }
    }

    private static void populateJiraCloudFields(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        JiraCloudJobDetailsEntity jiraCloudJobDetails = jobEntity.getJiraCloudJobDetails();
        if (null != jiraCloudJobDetails) {
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.cloud.add.comments", jiraCloudJobDetails.getAddComments().toString()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.cloud.issue.creator", jiraCloudJobDetails.getIssueCreatorEmail()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.cloud.project.name", jiraCloudJobDetails.getProjectNameOrKey()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.cloud.issue.type", jiraCloudJobDetails.getIssueType()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.cloud.resolve.workflow", jiraCloudJobDetails.getResolveTransition()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.cloud.reopen.workflow", jiraCloudJobDetails.getReopenTransition()));
        }
    }

    private static void populateJiraServerFields(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        JiraServerJobDetailsEntity jiraServerJobDetails = jobEntity.getJiraServerJobDetails();
        if (null != jiraServerJobDetails) {
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.server.add.comments", jiraServerJobDetails.getAddComments().toString()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.server.issue.creator", jiraServerJobDetails.getIssueCreatorUsername()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.server.project.name", jiraServerJobDetails.getProjectNameOrKey()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.server.issue.type", jiraServerJobDetails.getIssueType()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.server.resolve.workflow", jiraServerJobDetails.getResolveTransition()));
            channelConfigurationModel.put(createConfigFieldModel("channel.jira.server.reopen.workflow", jiraServerJobDetails.getReopenTransition()));
        }
    }

    private static void populateMSTeamsField(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        Optional.ofNullable(jobEntity.getMsTeamsJobDetails())
            .map(MSTeamsJobDetailsEntity::getWebhook)
            .map(webhook -> createConfigFieldModel("channel.msteams.webhook", webhook))
            .ifPresent(channelConfigurationModel::put);
    }

    private static void populateSlackFields(DistributionJobEntity jobEntity, ConfigurationModelMutable channelConfigurationModel) {
        SlackJobDetailsEntity slackJobDetails = jobEntity.getSlackJobDetails();
        if (null != slackJobDetails) {
            channelConfigurationModel.put(createConfigFieldModel("channel.slack.webhook", slackJobDetails.getWebhook()));
            channelConfigurationModel.put(createConfigFieldModel("channel.slack.channel.name", slackJobDetails.getChannelName()));
            channelConfigurationModel.put(createConfigFieldModel("channel.slack.channel.username", slackJobDetails.getChannelUsername()));
        }
    }

}
