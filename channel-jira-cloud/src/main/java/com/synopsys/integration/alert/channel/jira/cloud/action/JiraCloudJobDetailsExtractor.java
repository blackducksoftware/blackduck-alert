/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.action.JiraJobDetailsExtractor;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

@Component
public class JiraCloudJobDetailsExtractor extends JiraJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    public JiraCloudJobDetailsExtractor(JiraCloudChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor, Gson gson) {
        super(channelKey, fieldExtractor, gson);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public DistributionJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new JiraCloudJobDetailsModel(
            jobId,
            fieldExtractor.extractFieldValue(JiraCloudDescriptor.KEY_ADD_COMMENTS, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            fieldExtractor.extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_ISSUE_CREATOR, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_ISSUE_TYPE, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractJiraFieldMappings(JiraCloudDescriptor.KEY_FIELD_MAPPING, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_ISSUE_SUMMARY, configuredFieldsMap)
        );
    }

}
