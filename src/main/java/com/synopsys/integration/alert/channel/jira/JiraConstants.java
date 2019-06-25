/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.jira;

public class JiraConstants {
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.key.
    public static final String JIRA_ISSUE_PROPERTY_KEY = "com-synopsys-integration-alert";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_BOM_COMPONENT_URI = "bomComponentUri";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY = "category";
    // This String must always match the String found in the atlassian-connect.json file under modules.jiraEntityProperties.keyConfigurations.propertyKey["com-synopsys-integration-alert"].extractions.objectName.
    public static final String JIRA_ISSUE_PROPERTY_OBJECT_KEY_POLICY_NAME = "policyName";

}
