/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.action.JiraGlobalFieldModelTestAction;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.cloud.service.MyPermissionsService;
import com.blackduck.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.blackduck.integration.jira.common.model.response.PermissionModel;
import com.blackduck.integration.jira.common.rest.service.PluginManagerService;
import com.google.gson.Gson;

@Component
public class JiraCloudGlobalFieldModelTestAction extends JiraGlobalFieldModelTestAction {
    public static final Logger logger = LoggerFactory.getLogger(JiraCloudGlobalFieldModelTestAction.class);
    private final JiraCloudPropertiesFactory jiraCloudPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraCloudGlobalFieldModelTestAction(JiraCloudPropertiesFactory jiraCloudPropertiesFactory, Gson gson) {
        this.jiraCloudPropertiesFactory = jiraCloudPropertiesFactory;
        this.gson = gson;
    }

    @Override
    protected boolean isAppCheckEnabled(FieldUtility fieldUtility) {
        // Keeping this two lines to improve readability (storing one bit until GC won't kill us)
        boolean isPluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return !isPluginCheckDisabled;
    }

    @Override
    protected boolean isAppMissing(FieldUtility fieldUtility) throws IntegrationException {
        JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties(fieldUtility);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        PluginManagerService jiraAppService = jiraCloudServiceFactory.createPluginManagerService();
        return !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    @Override
    protected boolean canUserGetIssues(FieldUtility fieldUtility) throws IntegrationException {
        JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties(fieldUtility);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssuePage("", null, 1);
        return null != issueSearchResponseModel.getIssues();
    }

    @Override
    protected boolean isUserAdmin(FieldUtility fieldUtility) throws IntegrationException {
        JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties(fieldUtility);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        MyPermissionsService myPermissionsService = jiraCloudServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions(JiraGlobalFieldModelTestAction.JIRA_ADMIN_PERMISSION_NAME);
        PermissionModel adminPermission = myPermissions.extractPermission(JiraGlobalFieldModelTestAction.JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }

    @Override
    protected String getChannelDisplayName() {
        return JiraCloudDescriptor.JIRA_LABEL;
    }

}
