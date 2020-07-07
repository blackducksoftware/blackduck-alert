/**
 * alert-jira
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
package com.synopsys.integration.alert.jira.server;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerServiceConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.jira.common.rest.JiraHttpClient;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfigBuilder;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.log.Slf4jIntLogger;

public class JiraServerProperties implements IssueTrackerServiceConfig {
    public static final String KEY_ADD_COMMENTS = "jira.server.add.comments";
    public static final String KEY_ISSUE_CREATOR = "jira.server.issue.creator";
    public static final String KEY_JIRA_PROJECT_NAME = "jira.server.project.name";
    public static final String KEY_ISSUE_TYPE = "jira.server.issue.type";
    public static final String KEY_RESOLVE_WORKFLOW_TRANSITION = "jira.server.resolve.workflow";
    public static final String KEY_OPEN_WORKFLOW_TRANSITION = "jira.server.reopen.workflow";

    private final String url;
    private final String password;
    private final String username;

    public JiraServerProperties(String url, String password, String username) {
        this.url = url;
        this.password = password;
        this.username = username;
    }

    public JiraServerRestConfig createJiraServerConfig() throws IssueTrackerException {
        JiraServerRestConfigBuilder jiraServerConfigBuilder = new JiraServerRestConfigBuilder();

        jiraServerConfigBuilder.setUrl(url);
        jiraServerConfigBuilder.setAuthPassword(password);
        jiraServerConfigBuilder.setAuthUsername(username);
        try {
            return jiraServerConfigBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new IssueTrackerException("There was an issue building the configuration: " + e.getMessage());
        }
    }

    public JiraServerServiceFactory createJiraServicesServerFactory(Logger logger, Gson gson) throws IssueTrackerException {
        JiraServerRestConfig jiraServerConfig = createJiraServerConfig();
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        JiraHttpClient jiraHttpClient = jiraServerConfig.createJiraHttpClient(intLogger);
        return new JiraServerServiceFactory(intLogger, jiraHttpClient, gson);
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
