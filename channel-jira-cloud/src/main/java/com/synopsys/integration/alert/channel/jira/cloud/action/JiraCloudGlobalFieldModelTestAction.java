/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.api.channel.jira.action.JiraGlobalFieldModelTestAction;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.MyPermissionsService;
import com.synopsys.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.synopsys.integration.jira.common.model.response.PermissionModel;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

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
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssuePage("", 0, 1);
        return issueSearchResponseModel.getIssues().size() > 0;
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
