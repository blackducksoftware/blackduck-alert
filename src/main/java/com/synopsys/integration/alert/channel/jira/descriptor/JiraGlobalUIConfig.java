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
package com.synopsys.integration.alert.channel.jira.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class JiraGlobalUIConfig extends UIConfig {
    public static final String LABEL_URL = "Url";
    public static final String LABEL_USER_NAME = "Username";
    public static final String LABEL_ACCESS_TOKEN = "Access Token";

    public static final String DESCRIPTION_URL = "The URL of the Jira server.";
    public static final String DESCRIPTION_USER_NAME = "The email used to log into the Jira server that has generated the access token.";
    public static final String DESCRIPTION_ACCESS_TOKEN = "The access token used to send API requests to the Jira server.";

    public JiraGlobalUIConfig() {
        super(JiraDescriptor.JIRA_LABEL, JiraDescriptor.JIRA_DESCRIPTION, JiraDescriptor.JIRA_URL, JiraDescriptor.JIRA_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField jiraUrl = TextInputConfigField.createRequired(JiraDescriptor.KEY_JIRA_URL, LABEL_URL, DESCRIPTION_URL);
        final ConfigField jiraUserName = TextInputConfigField.createRequired(JiraDescriptor.KEY_JIRA_USERNAME, LABEL_USER_NAME, DESCRIPTION_USER_NAME);
        final ConfigField jiraAccessToken = PasswordConfigField.createRequired(JiraDescriptor.KEY_JIRA_ACCESS_TOKEN, LABEL_ACCESS_TOKEN, DESCRIPTION_ACCESS_TOKEN);

        return List.of(jiraUrl, jiraUserName, jiraAccessToken);
    }
}
