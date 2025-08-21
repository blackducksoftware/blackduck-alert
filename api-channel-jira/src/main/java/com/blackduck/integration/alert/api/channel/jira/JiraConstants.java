/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira;

import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssuePropertyKeys;

public final class JiraConstants {
    public static final String DEFAULT_ISSUE_TYPE = "Task";
    // This String must always match the String found in the atlassian-connect.json file under key.
    public static final String JIRA_APP_KEY = "com.blackduck.integration.alert";
    public static final String JIRA_ALERT_APP_NAME = "Alert Issue Property Indexer";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-blackduck-integration-alert";
    // This String is used to support backwards compatability prior to rebranding in 8.0.0.
    public static final String JIRA_ISSUE_PROPERTY_OLD_KEY = "com-synopsys-integration-alert";

    public static final String JIRA_SEARCH_KEY_JIRA_PROJECT = "project";

    // find tickets created by alert first:
    // 1. A summary that starts with "Alert - Black Duck"
    // 2. A summary that isn't an Alert test message and does not have a comment "=== BEGIN JIRA ISSUE KEYS ==="
    // 3. Then check if the new property key exists on that issue or if the old property key is on the issue.
    public static final String JQL_QUERY_FOR_ISSUE_SEARCH_COMMENT_MIGRATION = String.format("(summary ~ \"Alert - Black Duck\" OR (summary !~ \"Alert Test Message\" AND NOT comment ~\"%s\")) AND (issue.property[%s].topicName IS NOT EMPTY OR issue.property[%s].topicName IS NOT EMPTY) ORDER BY created DESC", JiraIssuePropertyKeys.JIRA_ISSUE_KEY_START_HEADER, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY);

    // These Strings must always match the Strings found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-blackduck-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER = "provider";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER_URL = "providerUrl";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_TOPIC_NAME = "topicName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_TOPIC_VALUE = "topicValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_TOPIC_NAME = "subTopicName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_TOPIC_VALUE = "subTopicValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY = "category";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_NAME = "componentName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_VALUE = "componentValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_NAME = "subComponentName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_VALUE = "subComponentValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY = "additionalKey";

    public static final String JIRA_ISSUE_VALIDATION_ERROR_MESSAGE = "There are issues with the configuration.";

    private JiraConstants() {
    }

}
