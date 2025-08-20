/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

public final class JiraIssuePropertyKeys {
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-blackduck-integration-alert";
    // These Strings must always match the Strings found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-blackduck-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER = "provider";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER_URL = "providerUrl";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_LABEL = "topicName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_NAME = "topicValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_LABEL = "subTopicName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_NAME = "subTopicValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY = "category";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_LABEL = "componentName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_VALUE = "componentValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_NAME = "subComponentName";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_VALUE = "subComponentValue";
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY = "additionalKey";

    public static final String JIRA_ISSUE_KEY_SEPARATOR = ": ";
    public static final String JIRA_ISSUE_KEY_START_HEADER = "=== BEGIN JIRA ISSUE KEYS ===";
    public static final String JIRA_ISSUE_KEY_END_HEADER = "=== END JIRA ISSUE KEYS ===";
    public static final String JIRA_ISSUE_KEY_PROJECT_ID = "projectId";
    public static final String JIRA_ISSUE_KEY_PROJECT_VERSION_ID = "projectVersionId";
    public static final String JIRA_ISSUE_KEY_COMPONENT_NAME = "componentName";
    public static final String JIRA_ISSUE_KEY_COMPONENT_VERSION_NAME = "componentVersionName";
    public static final String JIRA_ISSUE_KEY_CATEGORY = "category";
    public static final String JIRA_ISSUE_KEY_POLICY_NAME = "policyName";

    private JiraIssuePropertyKeys() {
    }

}
