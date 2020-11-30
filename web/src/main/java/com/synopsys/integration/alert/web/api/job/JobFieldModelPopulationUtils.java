/**
 * web
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
package com.synopsys.integration.alert.web.api.job;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

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
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;

public class JobFieldModelPopulationUtils {
    public static JobFieldModel createJobFieldModel(DistributionJobModel jobModel) {
        FieldModel providerFieldModel = new FieldModel(new BlackDuckProviderKey().getUniversalKey(), ConfigContextEnum.DISTRIBUTION.name(), new HashMap<>());
        populateProviderFields(providerFieldModel, jobModel);

        FieldModel channelFieldModel = new FieldModel(jobModel.getChannelDescriptorName(), ConfigContextEnum.DISTRIBUTION.name(), new HashMap<>());
        populateChannelFields(channelFieldModel, jobModel);

        String jobIdString = Optional.ofNullable(jobModel.getJobId())
                                 .map(UUID::toString)
                                 .orElse(null);
        return new JobFieldModel(jobIdString, Set.of(providerFieldModel, channelFieldModel));
    }

    public static void populateProviderFields(FieldModel providerFieldModel, DistributionJobModel jobModel) {
        String providerCommonConfigId = Optional.ofNullable(jobModel.getBlackDuckGlobalConfigId())
                                            .map(String::valueOf)
                                            .orElse(null);
        putField(providerFieldModel, "provider.common.config.id", providerCommonConfigId);
        putField(providerFieldModel, "provider.distribution.processing.type", jobModel.getProcessingType().toString());
        putField(providerFieldModel, "provider.distribution.notification.types", jobModel.getNotificationTypes());

        boolean filterByProject = jobModel.isFilterByProject();
        putField(providerFieldModel, "channel.common.filter.by.project", Boolean.toString(filterByProject));

        if (filterByProject) {
            jobModel.getProjectNamePattern()
                .filter(StringUtils::isNotBlank)
                .ifPresent(pattern -> putField(providerFieldModel, "channel.common.project.name.pattern", pattern));

            List<String> blackDuckProjectNames = jobModel.getProjectFilterProjectNames();
            if (!blackDuckProjectNames.isEmpty()) {
                putField(providerFieldModel, "channel.common.configured.project", blackDuckProjectNames);
            }
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
        putField(channelFieldModel, "channel.common.enabled", Boolean.toString(jobModel.isEnabled()));
        putField(channelFieldModel, "channel.common.name", jobModel.getName());
        putField(channelFieldModel, "channel.common.channel.name", channelDescriptorName);
        putField(channelFieldModel, "channel.common.frequency", jobModel.getDistributionFrequency().name());
        putField(channelFieldModel, "provider.distribution.processing.type", jobModel.getProcessingType().name());

        DistributionJobDetailsModel jobDetails = jobModel.getDistributionJobDetails();
        if (jobDetails.isAzureBoardsDetails()) {
            populateAzureBoardsFields(channelFieldModel, jobDetails.getAsAzureBoardsJobDetails());
        } else if (jobDetails.isEmailDetails()) {
            populateEmailFields(channelFieldModel, jobDetails.getAsEmailJobDetails());
        } else if (jobDetails.isJiraCloudDetails()) {
            populateJiraCloudFields(channelFieldModel, jobDetails.getAsJiraCouldJobDetails());
        } else if (jobDetails.isJiraServerDetails()) {
            populateJiraServerFields(channelFieldModel, jobDetails.getAsJiraServerJobDetails());
        } else if (jobDetails.isMSTeamsDetails()) {
            populateMSTeamsField(channelFieldModel, jobDetails.getAsMSTeamsJobDetails());
        } else if (jobDetails.isSlackDetails()) {
            populateSlackFields(channelFieldModel, jobDetails.getAsSlackJobDetails());
        }
    }

    private static void populateAzureBoardsFields(FieldModel channelFieldModel, AzureBoardsJobDetailsModel azureBoardsJobDetails) {
        if (null != azureBoardsJobDetails) {
            putField(channelFieldModel, "channel.azure.boards.work.item.comment", Boolean.toString(azureBoardsJobDetails.isAddComments()));
            putField(channelFieldModel, "channel.azure.boards.project", azureBoardsJobDetails.getProjectNameOrId());
            putField(channelFieldModel, "channel.azure.boards.work.item.type", azureBoardsJobDetails.getWorkItemType());
            putField(channelFieldModel, "channel.azure.boards.work.item.completed.state", azureBoardsJobDetails.getWorkItemCompletedState());
            putField(channelFieldModel, "channel.azure.boards.work.item.reopen.state", azureBoardsJobDetails.getWorkItemReopenState());
        }
    }

    private static void populateEmailFields(FieldModel channelFieldModel, EmailJobDetailsModel emailJobDetails) {
        if (null != emailJobDetails) {
            putField(channelFieldModel, "email.subject.line", emailJobDetails.getSubjectLine());
            putField(channelFieldModel, "email.attachment.format", emailJobDetails.getAttachmentFileType());
            putField(channelFieldModel, "project.owner.only", Boolean.toString(emailJobDetails.isProjectOwnerOnly()));
            putField(channelFieldModel, "email.additional.addresses.only", Boolean.toString(emailJobDetails.isAdditionalEmailAddressesOnly()));

            List<String> emailAddresses = emailJobDetails.getAdditionalEmailAddresses();
            if (!emailAddresses.isEmpty()) {
                putField(channelFieldModel, "email.additional.addresses", emailAddresses);
            }
        }
    }

    private static void populateJiraCloudFields(FieldModel channelFieldModel, JiraCloudJobDetailsModel jiraCloudJobDetails) {
        if (null != jiraCloudJobDetails) {
            putField(channelFieldModel, "channel.jira.cloud.add.comments", Boolean.toString(jiraCloudJobDetails.isAddComments()));
            putField(channelFieldModel, "channel.jira.cloud.issue.creator", jiraCloudJobDetails.getIssueCreatorEmail());
            putField(channelFieldModel, "channel.jira.cloud.project.name", jiraCloudJobDetails.getProjectNameOrKey());
            putField(channelFieldModel, "channel.jira.cloud.issue.type", jiraCloudJobDetails.getIssueType());
            putField(channelFieldModel, "channel.jira.cloud.resolve.workflow", jiraCloudJobDetails.getResolveTransition());
            putField(channelFieldModel, "channel.jira.cloud.reopen.workflow", jiraCloudJobDetails.getReopenTransition());
        }
    }

    private static void populateJiraServerFields(FieldModel channelFieldModel, JiraServerJobDetailsModel jiraServerJobDetails) {
        if (null != jiraServerJobDetails) {
            putField(channelFieldModel, "channel.jira.server.add.comments", Boolean.toString(jiraServerJobDetails.isAddComments()));
            putField(channelFieldModel, "channel.jira.server.issue.creator", jiraServerJobDetails.getIssueCreatorUsername());
            putField(channelFieldModel, "channel.jira.server.project.name", jiraServerJobDetails.getProjectNameOrKey());
            putField(channelFieldModel, "channel.jira.server.issue.type", jiraServerJobDetails.getIssueType());
            putField(channelFieldModel, "channel.jira.server.resolve.workflow", jiraServerJobDetails.getResolveTransition());
            putField(channelFieldModel, "channel.jira.server.reopen.workflow", jiraServerJobDetails.getReopenTransition());
        }
    }

    private static void populateMSTeamsField(FieldModel channelFieldModel, MSTeamsJobDetailsModel msTeamsJobDetails) {
        if (null != msTeamsJobDetails) {
            putField(channelFieldModel, "channel.msteams.webhook", msTeamsJobDetails.getWebhook());
        }
    }

    private static void populateSlackFields(FieldModel channelFieldModel, SlackJobDetailsModel slackJobDetails) {
        if (null != slackJobDetails) {
            putField(channelFieldModel, "channel.slack.webhook", slackJobDetails.getWebhook());
            putField(channelFieldModel, "channel.slack.channel.name", slackJobDetails.getChannelName());
            putField(channelFieldModel, "channel.slack.channel.username", slackJobDetails.getChannelUsername());
        }
    }

    private static void putField(FieldModel fieldModel, String key, String value) {
        putField(fieldModel, key, List.of(value));
    }

    private static void putField(FieldModel fieldModel, String key, List<String> values) {
        FieldValueModel fieldValueModel = new FieldValueModel(values, true);
        fieldModel.putField(key, fieldValueModel);
    }

}
