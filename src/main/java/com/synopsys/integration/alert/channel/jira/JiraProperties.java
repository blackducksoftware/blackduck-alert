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
package com.synopsys.integration.alert.channel.jira;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.jira.common.cloud.configuration.JiraServerConfig;
import com.synopsys.integration.jira.common.cloud.configuration.JiraServerConfigBuilder;
import com.synopsys.integration.jira.common.cloud.rest.JiraCloudHttpClient;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.log.Slf4jIntLogger;

public class JiraProperties {
    private final String url;
    private final String accessToken;
    private final String username;

    public JiraProperties(final FieldAccessor fieldAccessor) {
        url = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_URL).orElse(null);
        accessToken = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_ADMIN_API_TOKEN).orElse(null);
        username = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS).orElse(null);
    }

    public JiraProperties(final String url, final String accessToken, final String username) {
        this.url = url;
        this.accessToken = accessToken;
        this.username = username;
    }

    public JiraServerConfig createJiraServerConfig() throws AlertException {
        final JiraServerConfigBuilder jiraServerConfigBuilder = new JiraServerConfigBuilder();

        jiraServerConfigBuilder.setUrl(url);
        jiraServerConfigBuilder.setApiToken(accessToken);
        jiraServerConfigBuilder.setAuthUserEmail(username);
        try {
            return jiraServerConfigBuilder.build();
        } catch (final IllegalArgumentException e) {
            throw new AlertException("There was an issue building the configuration: " + e.getMessage());
        }
    }

    public JiraCloudServiceFactory createJiraServicesCloudFactory(final Logger logger, final Gson gson) throws AlertException {
        final JiraServerConfig jiraServerConfig = createJiraServerConfig();
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        final JiraCloudHttpClient jiraHttpClient = jiraServerConfig.createJiraHttpClient(intLogger);
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
