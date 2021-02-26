/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.actions;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.actions.JiraJobDetailsExtractor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerJobDetailsExtractor extends JiraJobDetailsExtractor {
    @Autowired
    public JiraServerJobDetailsExtractor(Gson gson) {
        super(gson);
    }

    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new JiraServerJobDetailsModel(
            jobId,
            extractFieldValue(JiraServerDescriptor.KEY_ADD_COMMENTS, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_CREATOR, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_TYPE, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractJiraFieldMappings(JiraServerDescriptor.KEY_FIELD_MAPPING, configuredFieldsMap)
        );
    }

}
