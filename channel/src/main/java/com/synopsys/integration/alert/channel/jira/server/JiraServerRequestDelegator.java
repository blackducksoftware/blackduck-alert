/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.JiraCustomFieldResolver;
import com.synopsys.integration.alert.channel.jira.common.util.JiraContentValidator;
import com.synopsys.integration.alert.channel.jira.common.util.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerIssueHandler;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerIssuePropertyHandler;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerTransitionHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

public class JiraServerRequestDelegator {
    public static final String ERROR_MESSAGE_PLUGIN_CONFIG = "Please configure the Jira Server plugin for your instance via the global Jira Server channel settings.";
    private final Logger logger = LoggerFactory.getLogger(JiraServerRequestDelegator.class);

    private final Gson gson;
    private final IssueTrackerContext context;

    public JiraServerRequestDelegator(Gson gson, IssueTrackerContext context) {
        this.gson = gson;
        this.context = context;
    }

    public IssueTrackerResponse sendRequests(List<IssueTrackerRequest> requests) throws IntegrationException {
        if (null == context) {
            throw new IssueTrackerException("Context missing. Cannot determine Jira Server instance.");
        }
        if (null == requests || requests.isEmpty()) {
            throw new IssueTrackerException("Requests missing. Require at least one request.");
        }
        JiraServerProperties jiraProperties = (JiraServerProperties) context.getIssueTrackerConfig();
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);

        if (!jiraProperties.isPluginCheckDisabled()) {
            checkIfAlertPluginIsInstalled(jiraServerServiceFactory.createPluginManagerService());
        }

        ProjectService projectService = jiraServerServiceFactory.createProjectService();
        UserSearchService userSearchService = jiraServerServiceFactory.createUserSearchService();
        IssueTypeService issueTypeService = jiraServerServiceFactory.createIssueTypeService();
        IssueMetaDataService issueMetaDataService = jiraServerServiceFactory.createIssueMetadataService();

        JiraServerIssueConfigValidator jiraIssueConfigValidator = new JiraServerIssueConfigValidator(projectService, userSearchService, issueTypeService, issueMetaDataService);
        IssueConfig validIssueConfig = jiraIssueConfigValidator.createValidIssueConfig(context);

        IssueService issueService = jiraServerServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        FieldService fieldService = jiraServerServiceFactory.createFieldService();

        JiraContentValidator jiraContentValidator = new JiraContentValidator();
        JiraServerTransitionHandler jiraTransitionHandler = new JiraServerTransitionHandler(issueService);
        JiraServerIssuePropertyHandler jiraIssuePropertyHandler = new JiraServerIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
        JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(gson);
        JiraServerIssueHandler jiraIssueHandler = new JiraServerIssueHandler(issueService, jiraProperties, jiraErrorMessageUtility, jiraTransitionHandler, jiraIssuePropertyHandler, jiraContentValidator, jiraCustomFieldResolver);
        return jiraIssueHandler.createOrUpdateIssues(validIssueConfig, requests);
    }

    private void checkIfAlertPluginIsInstalled(PluginManagerService jiraAppService) throws IssueTrackerException {
        logger.debug("Verifying the required application is installed on the Jira server...");

        try {
            boolean missingApp = !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
            if (missingApp) {
                throw new IssueTrackerException(ERROR_MESSAGE_PLUGIN_CONFIG);
            }
        } catch (IntegrationException ex) {
            throw new IssueTrackerException(ERROR_MESSAGE_PLUGIN_CONFIG, ex);
        }
    }

}
