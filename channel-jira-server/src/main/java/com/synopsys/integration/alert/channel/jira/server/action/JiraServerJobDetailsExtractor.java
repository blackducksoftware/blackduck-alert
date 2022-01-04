/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.action.JiraJobDetailsExtractor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

@Component
public class JiraServerJobDetailsExtractor extends JiraJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;

    @Autowired
    public JiraServerJobDetailsExtractor(JiraServerChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor, Gson gson) {
        super(channelKey, fieldExtractor, gson);
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public DistributionJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new JiraServerJobDetailsModel(
            jobId,
            fieldExtractor.extractFieldValue(JiraServerDescriptor.KEY_ADD_COMMENTS, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            fieldExtractor.extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_CREATOR, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_TYPE, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractJiraFieldMappings(JiraServerDescriptor.KEY_FIELD_MAPPING, configuredFieldsMap),
            fieldExtractor.extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_SUMMARY, configuredFieldsMap)
        );
    }

}
