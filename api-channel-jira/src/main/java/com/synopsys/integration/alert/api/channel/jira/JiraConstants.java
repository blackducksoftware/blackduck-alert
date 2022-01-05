/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira;

public final class JiraConstants {
    public static final String DEFAULT_ISSUE_TYPE = "Task";
    // This String must always match the String found in the atlassian-connect.json file under key.
    public static final String JIRA_APP_KEY = "com.synopsys.integration.alert";
    public static final String JIRA_ALERT_APP_NAME = "Alert Issue Property Indexer";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-synopsys-integration-alert";

    public static final String JIRA_SEARCH_KEY_JIRA_PROJECT = "project";

    // These Strings must always match the Strings found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
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
