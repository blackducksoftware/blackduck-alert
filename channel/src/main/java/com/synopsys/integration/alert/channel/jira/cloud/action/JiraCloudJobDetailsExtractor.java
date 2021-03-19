/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.action;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.action.JiraJobDetailsExtractor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

@Component
public class JiraCloudJobDetailsExtractor extends JiraJobDetailsExtractor {
    @Autowired
    public JiraCloudJobDetailsExtractor(Gson gson) {
        super(gson);
    }

    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new JiraCloudJobDetailsModel(
            jobId,
            extractFieldValue(JiraCloudDescriptor.KEY_ADD_COMMENTS, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_ISSUE_CREATOR, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_ISSUE_TYPE, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractJiraFieldMappings(JiraCloudDescriptor.KEY_FIELD_MAPPING, configuredFieldsMap)
        );
    }

}
