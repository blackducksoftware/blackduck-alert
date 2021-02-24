/*
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
package com.synopsys.integration.alert.channel.jira2.common;

public final class JiraIssuePropertyKeys {
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-synopsys-integration-alert";
    // These Strings must always match the Strings found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
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

    private JiraIssuePropertyKeys() {
    }

}
