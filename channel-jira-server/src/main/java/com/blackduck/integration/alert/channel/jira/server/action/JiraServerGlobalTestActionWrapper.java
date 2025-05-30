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

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.blackduck.integration.jira.common.model.response.PermissionModel;
import com.blackduck.integration.jira.common.rest.service.PluginManagerService;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.jira.common.server.service.MyPermissionsService;
import com.google.gson.Gson;

public class JiraServerGlobalTestActionWrapper {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraServerGlobalConfigModel jiraServerGlobalConfigModel;
    private final JiraServerServiceFactory jiraServerServiceFactory;

    public JiraServerGlobalTestActionWrapper(JiraServerPropertiesFactory jiraServerPropertiesFactory, Gson gson, JiraServerGlobalConfigModel jiraServerGlobalConfigModel) throws IssueTrackerException {
        this.jiraServerGlobalConfigModel = jiraServerGlobalConfigModel;
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(jiraServerGlobalConfigModel);
        this.jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
    }

    public boolean isAppCheckEnabled() {
        return !jiraServerGlobalConfigModel.getDisablePluginCheck().orElse(false);
    }

    public boolean isAppMissing() throws IntegrationException {
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        return !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    public boolean canUserGetIssues() throws IntegrationException {
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        // Updated the JQL to support customers using "Disable empty JQL queries" in Jira Server.
        // https://confluence.atlassian.com/adminjiraserver0820/configuring-jira-application-options-1095777704.html#settings:~:text=Disable%20empty%20JQL%20queries
        // if the user doesn't have permissions to get issues then an exception for a HTTP 403 forbidden is thrown.
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssuePage("created <= now()", 0, 1);
        return null != issueSearchResponseModel;
    }

    public boolean isUserAdmin() throws IntegrationException {
        MyPermissionsService myPermissionsService = jiraServerServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions();
        PermissionModel adminPermission = myPermissions.extractPermission(JiraServerGlobalTestAction.JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }
}
