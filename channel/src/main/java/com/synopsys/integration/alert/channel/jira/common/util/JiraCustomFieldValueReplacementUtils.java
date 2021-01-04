/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.common.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.model.JiraCustomFieldConfig;

public final class JiraCustomFieldValueReplacementUtils {
    public static final String REPLACEMENT_PROVIDER_NAME = "{{providerName}}";
    public static final String REPLACEMENT_PROJECT_NAME = "{{projectName}}";
    public static final String REPLACEMENT_PROJECT_VERSION = "{{projectVersion}}";
    public static final String REPLACEMENT_COMPONENT_NAME = "{{componentName}}";
    public static final String REPLACEMENT_COMPONENT_VERSION = "{{componentVersion}}";

    // "None" is a frequently used default String for many fields
    public static final String DEFAULT_REPLACEMENT = "None";

    public static void injectReplacementFieldValue(JiraCustomFieldConfig jiraCustomField, JiraIssueSearchProperties jiraIssueSearchProperties) {
        String originalValue = jiraCustomField.getFieldOriginalValue();
        extractReplacementValue(originalValue, jiraIssueSearchProperties)
            .ifPresent(jiraCustomField::setFieldReplacementValue);
    }

    private static Optional<String> extractReplacementValue(String originalFieldValue, JiraIssueSearchProperties jiraIssueSearchProperties) {
        switch (originalFieldValue) {
            case REPLACEMENT_PROVIDER_NAME:
                return Optional.of(jiraIssueSearchProperties.getProvider());
            case REPLACEMENT_PROJECT_NAME:
                return Optional.of(jiraIssueSearchProperties.getTopicValue());
            case REPLACEMENT_PROJECT_VERSION:
                return defaultIfBlank(jiraIssueSearchProperties.getSubTopicValue());
            case REPLACEMENT_COMPONENT_NAME:
                return defaultIfBlank(jiraIssueSearchProperties.getComponentValue());
            case REPLACEMENT_COMPONENT_VERSION:
                return defaultIfBlank(jiraIssueSearchProperties.getSubComponentValue());
            default:
                return Optional.empty();
        }
    }

    private static Optional<String> defaultIfBlank(String nullableValue) {
        return Optional.ofNullable(StringUtils.trimToNull(nullableValue))
                   .or(() -> Optional.of(DEFAULT_REPLACEMENT));
    }

}
