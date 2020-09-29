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
package com.synopsys.integration.alert.channel.jira.cloud.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.JiraGlobalTestAction;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.MyPermissionsService;
import com.synopsys.integration.jira.common.cloud.service.UserSearchService;
import com.synopsys.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.synopsys.integration.jira.common.model.response.PermissionModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

@Component
public class JiraCloudGlobalTestAction extends JiraGlobalTestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraCloudGlobalTestAction.class);
    private final Gson gson;

    @Autowired
    public JiraCloudGlobalTestAction(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected boolean isAppCheckEnabled(FieldAccessor fieldAccessor) {
        // Keeping this two lines to improve readability (storing one bit until GC won't kill us)
        boolean isPluginCheckDisabled = fieldAccessor.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return !isPluginCheckDisabled;
    }

    @Override
    protected boolean isAppMissing(FieldAccessor fieldAccessor) throws IntegrationException {
        JiraCloudProperties jiraProperties = createJiraProperties(fieldAccessor);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        PluginManagerService jiraAppService = jiraCloudServiceFactory.createPluginManagerService();
        String username = jiraProperties.getUsername();
        return !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    @Override
    protected boolean isUserMissing(FieldAccessor fieldAccessor) throws IntegrationException {
        JiraCloudProperties jiraProperties = createJiraProperties(fieldAccessor);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        UserSearchService userSearchService = jiraCloudServiceFactory.createUserSearchService();
        String username = jiraProperties.getUsername();
        return userSearchService.findUser(username).stream()
                   .map(UserDetailsResponseModel::getEmailAddress)
                   .noneMatch(email -> email.equals(username));
    }

    @Override
    protected boolean isUserAdmin(FieldAccessor fieldAccessor) throws IntegrationException {
        JiraCloudProperties jiraProperties = createJiraProperties(fieldAccessor);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        MyPermissionsService myPermissionsService = jiraCloudServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions(JiraGlobalTestAction.JIRA_ADMIN_PERMISSION_NAME);
        PermissionModel adminPermission = myPermissions.extractPermission(JiraGlobalTestAction.JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }

    private JiraCloudProperties createJiraProperties(FieldAccessor fieldAccessor) {
        String url = fieldAccessor.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_URL);
        String accessToken = fieldAccessor.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        String username = fieldAccessor.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        boolean isPluginCheckDisabled = fieldAccessor.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return new JiraCloudProperties(url, accessToken, username, isPluginCheckDisabled);
    }

    @Override
    protected String getChannelDisplayName() {
        return JiraCloudDescriptor.JIRA_LABEL;
    }

}
