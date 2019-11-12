package com.synopsys.integration.alert.issuetracker.jira.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.issuetracker.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.IssueTrackerService;
import com.synopsys.integration.alert.issuetracker.jira.common.JiraConstants;
import com.synopsys.integration.alert.issuetracker.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.issuetracker.jira.server.util.JiraServerIssueHandler;
import com.synopsys.integration.alert.issuetracker.jira.server.util.JiraServerIssuePropertyHandler;
import com.synopsys.integration.alert.issuetracker.jira.server.util.JiraServerTransitionHandler;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerMessageResult;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

public class JiraServerService extends IssueTrackerService {
    private Logger logger = LoggerFactory.getLogger(JiraServerService.class);

    public JiraServerService(Gson gson) {
        super(gson, new JiraMessageParser());
    }

    @Override
    public IssueTrackerMessageResult sendMessage(IssueTrackerRequest request) throws IntegrationException {
        IssueTrackerContext context = request.getContext();
        JiraServerProperties jiraProperties = (JiraServerProperties) request.getContext().getIssueTrackerConfig();
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, getGson());
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        logger.debug("Verifying the required application is installed on the Jira server...");
        boolean missingApp = jiraAppService.getInstalledApp(jiraProperties.getUsername(), jiraProperties.getPassword(), JiraConstants.JIRA_APP_KEY).isEmpty();
        if (missingApp) {
            throw new AlertException("Please configure the Jira Server plugin for your server instance via the global Jira Server channel settings.");
        }

        ProjectService projectService = jiraServerServiceFactory.createProjectService();
        UserSearchService userSearchService = jiraServerServiceFactory.createUserSearchService();
        IssueTypeService issueTypeService = jiraServerServiceFactory.createIssueTypeService();
        IssueMetaDataService issueMetaDataService = jiraServerServiceFactory.createIssueMetadataService();

        JiraServerIssueConfigValidator jiraIssueConfigValidator = new JiraServerIssueConfigValidator(projectService, userSearchService, issueTypeService, issueMetaDataService);
        jiraIssueConfigValidator.validate(context);

        IssueService issueService = jiraServerServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        JiraServerTransitionHandler jiraTransitionHandler = new JiraServerTransitionHandler(issueService);
        JiraServerIssuePropertyHandler jiraIssuePropertyHandler = new JiraServerIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraServerIssueHandler jiraIssueHandler = new JiraServerIssueHandler(issueService, jiraProperties, getMessageParser(), getGson(), jiraTransitionHandler, jiraIssuePropertyHandler);
        return jiraIssueHandler.createOrUpdateIssues(context.getIssueConfig(), request.getRequestContent());
    }
}
