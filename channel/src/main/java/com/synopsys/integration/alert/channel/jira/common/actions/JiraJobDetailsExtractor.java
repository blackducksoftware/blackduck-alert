/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.actions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsExtractor;

public abstract class JiraJobDetailsExtractor extends JobDetailsExtractor {
    private Gson gson;

    public JiraJobDetailsExtractor(Gson gson) {
        this.gson = gson;
    }

    protected List<JiraJobCustomFieldModel> extractJiraFieldMappings(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        List<String> fieldMappingStrings = extractFieldValues(fieldKey, configuredFieldsMap);
        return fieldMappingStrings.stream()
                   .map(fieldMappingString -> gson.fromJson(fieldMappingString, JiraJobCustomFieldModel.class))
                   .collect(Collectors.toList());
    }
}
