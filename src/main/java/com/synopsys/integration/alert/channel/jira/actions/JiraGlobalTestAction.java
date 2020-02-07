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
package com.synopsys.integration.alert.channel.jira.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.JiraConstants;
import com.synopsys.integration.alert.channel.jira.JiraProperties;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraAppService;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.rest.service.UserSearchService;

@Component
public class JiraGlobalTestAction extends TestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraGlobalTestAction.class);
    private final Gson gson;

    @Autowired
    public JiraGlobalTestAction(final Gson gson) {
        this.gson = gson;
    }

    @Override
    public String testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();
        final JiraProperties jiraProperties = new JiraProperties(fieldAccessor);
        try {
            final JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            final JiraAppService jiraAppService = jiraCloudServiceFactory.createJiraAppService();
            final String username = jiraProperties.getUsername();
            final boolean missingApp = jiraAppService.getInstalledApp(username, jiraProperties.getAccessToken(), JiraConstants.JIRA_APP_KEY).isEmpty();
            if (missingApp) {
                throw new AlertException("Please configure the Jira Cloud plugin for your server.");
            }
            final UserSearchService userSearchService = jiraCloudServiceFactory.createUserSearchService();
            final boolean retrievedCurrentUser = userSearchService.findUser(username).stream().map(UserDetailsResponseModel::getEmailAddress).anyMatch(email -> email.equals(username));
            if (!retrievedCurrentUser) {
                throw new AlertException("User did not match any known users.");
            }
        } catch (final IntegrationException e) {
            throw new AlertException("An error occurred during testing: " + e.getMessage());
        }
        return "Successfully connected to Jira Cloud instance.";
    }
}
