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
package com.synopsys.integration.alert.channel.jira.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.util.JiraContentValidator;
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
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

public class JiraServerRequestDelegator {
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
        PluginManagerService jiraAppService = jiraServerServiceFactory.createPluginManagerService();
        logger.debug("Verifying the required application is installed on the Jira server...");
        boolean missingApp = !jiraAppService.isAppInstalled(jiraProperties.getUsername(), jiraProperties.getPassword(), JiraConstants.JIRA_APP_KEY);
        if (missingApp) {
            throw new IssueTrackerException("Please configure the Jira Server plugin for your server instance via the global Jira Server channel settings.");
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
        JiraContentValidator jiraContentValidator = new JiraContentValidator();
        JiraServerTransitionHandler jiraTransitionHandler = new JiraServerTransitionHandler(issueService);
        JiraServerIssuePropertyHandler jiraIssuePropertyHandler = new JiraServerIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraServerIssueHandler jiraIssueHandler = new JiraServerIssueHandler(issueService, jiraProperties, gson, jiraTransitionHandler, jiraIssuePropertyHandler, jiraContentValidator);
        return jiraIssueHandler.createOrUpdateIssues(validIssueConfig, requests);
    }

}
