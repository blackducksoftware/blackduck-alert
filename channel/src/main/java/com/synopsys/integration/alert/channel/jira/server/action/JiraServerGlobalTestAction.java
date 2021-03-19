/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.action.JiraGlobalTestAction;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.synopsys.integration.jira.common.model.response.PermissionModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.MyPermissionsService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

@Component
public class JiraServerGlobalTestAction extends JiraGlobalTestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraServerGlobalTestAction.class);
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraServerGlobalTestAction(JiraServerPropertiesFactory jiraServerPropertiesFactory, Gson gson) {
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.gson = gson;
    }

    @Override
    protected boolean isAppCheckEnabled(FieldUtility fieldUtility) {
        boolean isPluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return !isPluginCheckDisabled;
    }

    @Override
    protected boolean isAppMissing(FieldUtility fieldUtility) throws IntegrationException {
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(fieldUtility);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        return !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    @Override
    protected boolean isUserMissing(FieldUtility fieldUtility) throws IntegrationException {
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(fieldUtility);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        UserSearchService userSearchService = jiraServerServiceFactory.createUserSearchService();
        String username = jiraProperties.getUsername();
        return userSearchService.findUserByUsername(username).stream().map(UserDetailsResponseModel::getName).noneMatch(email -> email.equals(username));
    }

    @Override
    protected boolean isUserAdmin(FieldUtility fieldUtility) throws IntegrationException {
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(fieldUtility);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        MyPermissionsService myPermissionsService = jiraServerServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions();
        PermissionModel adminPermission = myPermissions.extractPermission(JiraGlobalTestAction.JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }

    @Override
    protected String getChannelDisplayName() {
        return JiraServerDescriptor.JIRA_LABEL;
    }

}
