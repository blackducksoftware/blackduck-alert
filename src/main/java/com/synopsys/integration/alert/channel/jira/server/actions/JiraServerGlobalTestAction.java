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
package com.synopsys.integration.alert.channel.jira.server.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.JiraGlobalTestAction;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

@Component
public class JiraServerGlobalTestAction extends JiraGlobalTestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraServerGlobalTestAction.class);
    private final Gson gson;

    @Autowired
    public JiraServerGlobalTestAction(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected boolean isAppCheckEnabled(FieldAccessor fieldAccessor) {
        boolean isPluginCheckDisabled = fieldAccessor.getBooleanOrFalse(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return !isPluginCheckDisabled;
    }

    @Override
    protected boolean isAppMissing(FieldAccessor fieldAccessor) throws IntegrationException {
        JiraServerProperties jiraProperties = createProperties(fieldAccessor);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        String username = jiraProperties.getUsername();
        return !jiraAppService.isAppInstalled(username, jiraProperties.getPassword(), JiraConstants.JIRA_APP_KEY);
    }

    @Override
    protected boolean isUserMissing(FieldAccessor fieldAccessor) throws IntegrationException {
        JiraServerProperties jiraProperties = createProperties(fieldAccessor);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        UserSearchService userSearchService = jiraServerServiceFactory.createUserSearchService();
        String username = jiraProperties.getUsername();
        return userSearchService.findUserByUsername(username).stream().map(UserDetailsResponseModel::getName).noneMatch(email -> email.equals(username));
    }

    private JiraServerProperties createProperties(FieldAccessor fieldAccessor) {
        String url = fieldAccessor.getStringOrNull(JiraServerDescriptor.KEY_SERVER_URL);
        String username = fieldAccessor.getStringOrNull(JiraServerDescriptor.KEY_SERVER_USERNAME);
        String password = fieldAccessor.getStringOrNull(JiraServerDescriptor.KEY_SERVER_PASSWORD);
        boolean pluginCheckDisabled = fieldAccessor.getBooleanOrFalse(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return new JiraServerProperties(url, password, username, pluginCheckDisabled);
    }

    @Override
    protected String getChannelDisplayName() {
        return JiraServerDescriptor.JIRA_LABEL;
    }

}
