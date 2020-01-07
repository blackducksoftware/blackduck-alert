/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.alert.channel.jira.server.JiraServerChannelKey;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;

@Component
public class JiraServerDescriptor extends ChannelDescriptor {
    public static final String KEY_ADD_COMMENTS = "channel.jira.server.add.comments";
    public static final String KEY_ISSUE_CREATOR = "channel.jira.server.issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = "channel.jira.server.project.name";
    public static final String KEY_ISSUE_TYPE = "channel.jira.server.issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = "channel.jira.server.resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = "channel.jira.server.reopen.workflow";

    public static final String KEY_SERVER_URL = "jira.server.url";
    public static final String KEY_SERVER_USERNAME = "jira.server.username";
    public static final String KEY_SERVER_PASSWORD = "jira.server.password";
    public static final String KEY_JIRA_SERVER_CONFIGURE_PLUGIN = "jira.server.configure.plugin";

    public static final String JIRA_LABEL = "Jira Server";
    public static final String JIRA_URL = "jira_server";
    public static final String JIRA_DESCRIPTION = "Configure the Jira Server instance that Alert will send issue updates to.";

    @Autowired
    public JiraServerDescriptor(JiraServerChannelKey channelKey, JiraServerDistributionUIConfig jiraServerDistributionUIConfig, JiraServerGlobalUIConfig jiraServerGlobalUIConfig) {
        super(channelKey, jiraServerDistributionUIConfig, jiraServerGlobalUIConfig);
    }
}
