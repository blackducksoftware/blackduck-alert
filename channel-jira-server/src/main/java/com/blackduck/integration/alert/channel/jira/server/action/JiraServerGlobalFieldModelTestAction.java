/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.action.JiraGlobalFieldModelTestAction;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.blackduck.integration.jira.common.model.response.PermissionModel;
import com.blackduck.integration.jira.common.rest.service.PluginManagerService;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.jira.common.server.service.MyPermissionsService;
import com.google.gson.Gson;

/**
 * @deprecated Global test actions are now handled through JiraServerGlobalTestAction
 */
@Component
@Deprecated(forRemoval = true)
public class JiraServerGlobalFieldModelTestAction extends JiraGlobalFieldModelTestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraServerGlobalFieldModelTestAction.class);
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraServerGlobalFieldModelTestAction(JiraServerPropertiesFactory jiraServerPropertiesFactory, Gson gson) {
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
        JiraServerProperties jiraProperties = createJiraProperties(fieldUtility);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        return !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    @Override
    protected boolean canUserGetIssues(FieldUtility fieldUtility) throws IntegrationException {
        JiraServerProperties jiraProperties = createJiraProperties(fieldUtility);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssuePage("", 0, 1);
        return !issueSearchResponseModel.getIssues().isEmpty();
    }

    @Override
    protected boolean isUserAdmin(FieldUtility fieldUtility) throws IntegrationException {
        JiraServerProperties jiraProperties = createJiraProperties(fieldUtility);
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
        MyPermissionsService myPermissionsService = jiraServerServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions();
        PermissionModel adminPermission = myPermissions.extractPermission(JiraGlobalFieldModelTestAction.JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }

    @Override
    protected String getChannelDisplayName() {
        return JiraServerDescriptor.JIRA_LABEL;
    }

    private JiraServerProperties createJiraProperties(FieldUtility fieldUtility) {
        String url = fieldUtility.getStringOrNull(JiraServerDescriptor.KEY_SERVER_URL);
        // Legacy Jira Server endpoints do not support passing in timeouts. Usage of the old endpoints to create properties will set the timeout the default.
        Integer timeout = JiraServerPropertiesFactory.DEFAULT_JIRA_TIMEOUT_SECONDS;
        String username = fieldUtility.getStringOrNull(JiraServerDescriptor.KEY_SERVER_USERNAME);
        String password = fieldUtility.getStringOrNull(JiraServerDescriptor.KEY_SERVER_PASSWORD);
        boolean pluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return jiraServerPropertiesFactory.createJiraProperties(url, timeout, JiraServerAuthorizationMethod.BASIC, password, username, null, pluginCheckDisabled);
    }

}
