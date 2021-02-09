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
package com.synopsys.integration.alert.channel.jira.cloud.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudChannelKey;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;

@Component
public class JiraCloudDescriptor extends ChannelDescriptor {
    public static final String JIRA_CLOUD_PREFIX = "jira.cloud.";
    public static final String JIRA_CLOUD_CHANNEL_PREFIX = "channel." + JIRA_CLOUD_PREFIX;

    public static final String KEY_JIRA_URL = JIRA_CLOUD_PREFIX + "url";
    public static final String KEY_JIRA_ADMIN_EMAIL_ADDRESS = JIRA_CLOUD_PREFIX + "admin.email.address";
    public static final String KEY_JIRA_ADMIN_API_TOKEN = JIRA_CLOUD_PREFIX + "admin.api.token";
    public static final String KEY_JIRA_DISABLE_PLUGIN_CHECK = JIRA_CLOUD_PREFIX + "disable.plugin.check";
    public static final String KEY_JIRA_CONFIGURE_PLUGIN = JIRA_CLOUD_PREFIX + "configure.plugin";

    public static final String KEY_ADD_COMMENTS = JIRA_CLOUD_CHANNEL_PREFIX + "add.comments";
    public static final String KEY_ISSUE_CREATOR = JIRA_CLOUD_CHANNEL_PREFIX + "issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = JIRA_CLOUD_CHANNEL_PREFIX + "project.name";
    public static final String KEY_ISSUE_TYPE = JIRA_CLOUD_CHANNEL_PREFIX + "issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = JIRA_CLOUD_CHANNEL_PREFIX + "resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = JIRA_CLOUD_CHANNEL_PREFIX + "reopen.workflow";

    public static final String JIRA_LABEL = "Jira Cloud";
    public static final String JIRA_URL = "jira";
    public static final String JIRA_DESCRIPTION = "Configure the Jira Cloud instance that Alert will send issue updates to.";

    @Autowired
    public JiraCloudDescriptor(JiraCloudChannelKey jiraChannelKey, JiraCloudGlobalUIConfig globalUIConfig, JiraCloudDistributionUIConfig distributionUIConfig) {
        super(jiraChannelKey, distributionUIConfig, globalUIConfig);
    }

}
