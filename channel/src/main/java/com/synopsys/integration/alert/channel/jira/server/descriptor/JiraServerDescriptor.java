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
package com.synopsys.integration.alert.channel.jira.server.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraServerDescriptor extends ChannelDescriptor {
    public static final String JIRA_SERVER_PREFIX = "jira.server.";
    public static final String JIRA_SERVER_CHANNEL_PREFIX = "channel." + JIRA_SERVER_PREFIX;

    public static final String KEY_ADD_COMMENTS = JIRA_SERVER_CHANNEL_PREFIX + "add.comments";
    public static final String KEY_ISSUE_CREATOR = JIRA_SERVER_CHANNEL_PREFIX + "issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = JIRA_SERVER_CHANNEL_PREFIX + "project.name";
    public static final String KEY_ISSUE_TYPE = JIRA_SERVER_CHANNEL_PREFIX + "issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = JIRA_SERVER_CHANNEL_PREFIX + "resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = JIRA_SERVER_CHANNEL_PREFIX + "reopen.workflow";
    public static final String KEY_FIELD_MAPPING = JIRA_SERVER_CHANNEL_PREFIX + "field.mapping";

    public static final String KEY_SERVER_URL = JIRA_SERVER_PREFIX + "url";
    public static final String KEY_SERVER_USERNAME = JIRA_SERVER_PREFIX + "username";
    public static final String KEY_SERVER_PASSWORD = JIRA_SERVER_PREFIX + "password";
    public static final String KEY_JIRA_DISABLE_PLUGIN_CHECK = JIRA_SERVER_PREFIX + "disable.plugin.check";
    public static final String KEY_JIRA_SERVER_CONFIGURE_PLUGIN = JIRA_SERVER_PREFIX + "configure.plugin";

    public static final String JIRA_LABEL = "Jira Server";
    public static final String JIRA_URL = "jira_server";
    public static final String JIRA_DESCRIPTION = "Configure the Jira Server instance that Alert will send issue updates to.";

    @Autowired
    public JiraServerDescriptor(JiraServerDistributionUIConfig jiraServerDistributionUIConfig, JiraServerGlobalUIConfig jiraServerGlobalUIConfig) {
        super(ChannelKeys.JIRA_SERVER, jiraServerDistributionUIConfig, jiraServerGlobalUIConfig);
    }

}
