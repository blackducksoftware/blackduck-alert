/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.action;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

public abstract class JiraJobDetailsExtractor extends DistributionJobDetailsExtractor {
    private final DistributionJobFieldExtractor fieldExtractor;
    private final Gson gson;

    public JiraJobDetailsExtractor(IssueTrackerChannelKey channelKey, DistributionJobFieldExtractor fieldExtractor, Gson gson) {
        super(channelKey);
        this.fieldExtractor = fieldExtractor;
        this.gson = gson;
    }

    protected List<JiraJobCustomFieldModel> extractJiraFieldMappings(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        List<String> fieldMappingStrings = fieldExtractor.extractFieldValues(fieldKey, configuredFieldsMap);
        return fieldMappingStrings.stream()
            .map(fieldMappingString -> gson.fromJson(fieldMappingString, JiraJobCustomFieldModel.class))
            .collect(Collectors.toList());
    }

}
