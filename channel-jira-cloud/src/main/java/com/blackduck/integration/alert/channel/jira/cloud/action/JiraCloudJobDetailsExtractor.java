/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.jira.action.JiraJobDetailsExtractor;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.google.gson.Gson;

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
