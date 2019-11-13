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
package com.synopsys.integration.alert.issuetracker.jira.cloud;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerServiceConfig;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfig;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfigBuilder;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.rest.JiraHttpClient;
import com.synopsys.integration.log.Slf4jIntLogger;

public class JiraProperties implements IssueTrackerServiceConfig {
    public static final String KEY_JIRA_URL = "jira.cloud.url";
    public static final String KEY_JIRA_ADMIN_EMAIL_ADDRESS = "jira.cloud.admin.email.address";
    public static final String KEY_JIRA_ADMIN_API_TOKEN = "jira.cloud.admin.api.token";
    public static final String KEY_JIRA_CONFIGURE_PLUGIN = "jira.cloud.configure.plugin";

    public static final String KEY_ADD_COMMENTS = "channel.jira.cloud.add.comments";
    public static final String KEY_ISSUE_CREATOR = "channel.jira.cloud.issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = "channel.jira.cloud.project.name";
    public static final String KEY_ISSUE_TYPE = "channel.jira.cloud.issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = "channel.jira.cloud.resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = "channel.jira.cloud.reopen.workflow";

    private final String url;
    private final String accessToken;
    private final String username;

    public JiraProperties(String url, String accessToken, String username) {
        this.url = url;
        this.accessToken = accessToken;
        this.username = username;
    }

    public JiraCloudRestConfig createJiraServerConfig() throws AlertException {
        JiraCloudRestConfigBuilder jiraServerConfigBuilder = new JiraCloudRestConfigBuilder();

        jiraServerConfigBuilder.setUrl(url);
        jiraServerConfigBuilder.setApiToken(accessToken);
        jiraServerConfigBuilder.setAuthUserEmail(username);
        try {
            return jiraServerConfigBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new AlertException("There was an issue building the configuration: " + e.getMessage());
        }
    }

    public JiraCloudServiceFactory createJiraServicesCloudFactory(Logger logger, Gson gson) throws AlertException {
        JiraCloudRestConfig jiraServerConfig = createJiraServerConfig();
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        JiraHttpClient jiraHttpClient = jiraServerConfig.createJiraHttpClient(intLogger);
        return new JiraCloudServiceFactory(intLogger, jiraHttpClient, gson);
    }

    public String getUrl() {
        return url;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUsername() {
        return username;
    }

}
