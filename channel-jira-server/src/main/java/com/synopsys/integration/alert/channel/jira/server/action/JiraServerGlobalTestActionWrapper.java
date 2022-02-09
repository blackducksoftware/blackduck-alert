package com.synopsys.integration.alert.channel.jira.server.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.MultiPermissionResponseModel;
import com.synopsys.integration.jira.common.model.response.PermissionModel;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.MyPermissionsService;

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
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssuePage("", 0, 1);
        return !issueSearchResponseModel.getIssues().isEmpty();
    }

    public boolean isUserAdmin() throws IntegrationException {
        MyPermissionsService myPermissionsService = jiraServerServiceFactory.createMyPermissionsService();
        MultiPermissionResponseModel myPermissions = myPermissionsService.getMyPermissions();
        PermissionModel adminPermission = myPermissions.extractPermission(JiraServerGlobalTestAction.JIRA_ADMIN_PERMISSION_NAME);
        return null != adminPermission && adminPermission.getHavePermission();
    }
}
