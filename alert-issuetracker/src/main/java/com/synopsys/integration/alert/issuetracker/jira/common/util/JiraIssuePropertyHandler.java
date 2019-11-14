/**
 * alert-issuetracker
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.issuetracker.jira.common.util;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.issuetracker.IssueProperties;
import com.synopsys.integration.alert.issuetracker.jira.common.JiraConstants;
import com.synopsys.integration.exception.IntegrationException;

public abstract class JiraIssuePropertyHandler<T> {
    private static final String SEARCH_CONJUNCTION = "AND";
    private static final Set<Character> CHARACTERS_TO_ESCAPE = Set.of('\'');

    public abstract T queryForIssues(String query) throws IntegrationException;

    public abstract void addPropertiesToIssue(String issueKey, IssueProperties properties) throws IntegrationException;

    public Optional<T> findIssues(String jiraProjectKey, IssueProperties issueProperties) throws IntegrationException {
        String subTopicName = null;
        String subTopicValue = null;
        if (null != issueProperties.getSubTopicName()) {
            subTopicName = issueProperties.getSubTopicName();
            subTopicValue = issueProperties.getSubTopicValue();
        }

        if (null != issueProperties.getSubComponentName()) {
            String subComponentName = issueProperties.getSubComponentName();
            String subComponentValue = issueProperties.getSubComponentValue();

            return findIssues(
                jiraProjectKey, issueProperties.getProvider(), issueProperties.getTopicName(), issueProperties.getTopicValue(), subTopicName, subTopicValue, issueProperties.getCategory(), issueProperties.getComponentName(),
                issueProperties.getComponentValue(), subComponentName, subComponentValue, issueProperties.getAdditionalKey());
        } else {
            return findIssues(jiraProjectKey, issueProperties.getProvider(), issueProperties.getTopicName(), issueProperties.getTopicValue(), subTopicName, subTopicValue, null, null, null, null, null, issueProperties.getAdditionalKey());
        }
    }

    public Optional<T> findIssues(
        String jiraProjectKey,
        String provider,
        String topicName,
        String topicValue,
        String subTopicName,
        String subTopicValue,
        String category,
        String componentName,
        String componentValue,
        String subComponentName,
        String subComponentValue,
        String additionalKey
    ) throws IntegrationException {
        StringBuilder jqlBuilder = new StringBuilder();
        jqlBuilder.append(JiraConstants.JIRA_SEARCH_KEY_JIRA_PROJECT);
        jqlBuilder.append(" = '");
        jqlBuilder.append(escapeSearchString(jiraProjectKey));
        jqlBuilder.append("' ");

        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER, provider);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_TOPIC_NAME, topicName);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_TOPIC_VALUE, topicValue);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_TOPIC_NAME, subTopicName);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_TOPIC_VALUE, subTopicValue);

        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY, category);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_NAME, componentName);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_VALUE, componentValue);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_NAME, subComponentName);
        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_VALUE, subComponentValue);

        appendPropertySearchString(jqlBuilder, JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY, additionalKey);

        String jql = jqlBuilder.toString();
        if (!jql.isBlank()) {
            return Optional.of(queryForIssues(jql));
        }
        return Optional.empty();
    }

    private void appendPropertySearchString(StringBuilder jqlBuilder, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            jqlBuilder.append(SEARCH_CONJUNCTION);
            jqlBuilder.append(StringUtils.SPACE);
            jqlBuilder.append(createPropertySearchString(key, value));
            jqlBuilder.append(StringUtils.SPACE);
        }
    }

    private String createPropertySearchString(String key, String value) {
        String propertySearchFormat = "issue.property[%s].%s = '%s'";
        String escapedValue = escapeSearchString(value);
        return String.format(propertySearchFormat, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, key, escapedValue);
    }

    private String escapeSearchString(String originalString) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : originalString.toCharArray()) {
            // because the properties are now strings instead of text fields, we no longer need to escape the reserved characters
            // https://confluence.atlassian.com/jirasoftwarecloud/advanced-searching-764478330.html
            // we still need to escape single quotes in the string
            if (CHARACTERS_TO_ESCAPE.contains(character)) {
                stringBuilder.append("\\" + character);
            } else {
                stringBuilder.append(character);
            }
        }
        String escapedString = stringBuilder.toString();
        // if the string ends with a single backslash, we need to escape the single backslash
        if (escapedString.endsWith("\\") && !escapedString.endsWith("\\\\")) {
            stringBuilder.append("\\");
        }
        return stringBuilder.toString();
    }

}
