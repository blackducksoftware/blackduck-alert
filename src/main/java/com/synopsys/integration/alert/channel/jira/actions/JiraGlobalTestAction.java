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
package com.synopsys.integration.alert.channel.jira.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.configuration.JiraServerConfig;
import com.synopsys.integration.jira.common.cloud.configuration.JiraServerConfigBuilder;
import com.synopsys.integration.jira.common.cloud.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.JiraCloudHttpClient;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.rest.service.UserSearchService;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class JiraGlobalTestAction extends TestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraGlobalTestAction.class);
    private final Gson gson;

    @Autowired
    public JiraGlobalTestAction(final Gson gson) {
        this.gson = gson;
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();
        final JiraServerConfigBuilder jiraServerConfigBuilder = new JiraServerConfigBuilder();
        final String jiraUrl = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_URL).orElse(null);
        final String accessToken = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_ACCESS_TOKEN).orElse(null);
        final String username = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_USERNAME).orElse(null);
        jiraServerConfigBuilder.setUrl(jiraUrl);
        jiraServerConfigBuilder.setApiToken(accessToken);
        jiraServerConfigBuilder.setAuthUserEmail(username);
        try {
            final JiraServerConfig config = jiraServerConfigBuilder.build();
            final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
            final JiraCloudHttpClient jiraHttpClient = config.createJiraHttpClient(intLogger);
            final JiraCloudServiceFactory jiraCloudServiceFactory = new JiraCloudServiceFactory(intLogger, jiraHttpClient, gson);
            final UserSearchService userSearchService = jiraCloudServiceFactory.createUserSearchService();
            final boolean retrievedCurrentUser = userSearchService.findUser(username).stream().map(UserDetailsResponseModel::getEmailAddress).anyMatch(email -> email.equals(username));
            if (!retrievedCurrentUser) {
                throw new AlertException("User did not match any known users.");
            }
        } catch (final IllegalArgumentException e) {
            throw new AlertException("There was an issue building the configuration: " + e.getMessage());
        } catch (final IntegrationException e) {
            throw new AlertException("Was not able to retrieve User from Jira cloud instance: " + e.getMessage());
        }
    }
}
