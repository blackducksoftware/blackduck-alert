/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.cloud;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudIssueHandler;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudIssuePropertyHandler;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.util.JiraContentValidator;
import com.synopsys.integration.alert.channel.jira.common.util.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira2.common.JiraCustomFieldResolver;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.FieldService;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.service.UserSearchService;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

public class JiraCloudRequestDelegator {
    private final Logger logger = LoggerFactory.getLogger(JiraCloudRequestDelegator.class);

    private final Gson gson;
    private final IssueTrackerContext context;

    public JiraCloudRequestDelegator(Gson gson, IssueTrackerContext context) {
        this.gson = gson;
        this.context = context;
    }

    public IssueTrackerResponse sendRequests(List<IssueTrackerRequest> requests) throws IntegrationException {
        if (null == context) {
            throw new IssueTrackerException("Context missing. Cannot determine Jira Cloud instance.");
        }
        if (null == requests || requests.isEmpty()) {
            throw new IssueTrackerException("Requests missing. Requires at least one request.");
        }
        JiraCloudProperties jiraProperties = (JiraCloudProperties) context.getIssueTrackerConfig();
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);

        if (!jiraProperties.isPluginCheckDisabled()) {
            checkIfAlertPluginIsInstalled(jiraCloudServiceFactory.createPluginManagerService());
        }

        ProjectService projectService = jiraCloudServiceFactory.createProjectService();
        UserSearchService userSearchService = jiraCloudServiceFactory.createUserSearchService();
        IssueTypeService issueTypeService = jiraCloudServiceFactory.createIssueTypeService();
        IssueMetaDataService issueMetaDataService = jiraCloudServiceFactory.createIssueMetadataService();

        JiraCloudIssueConfigValidator jiraIssueConfigValidator = new JiraCloudIssueConfigValidator(projectService, userSearchService, issueTypeService, issueMetaDataService);
        IssueConfig validIssueConfig = jiraIssueConfigValidator.createValidIssueConfig(context);

        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
        FieldService fieldService = jiraCloudServiceFactory.createFieldService();

        JiraContentValidator contentValidator = new JiraContentValidator();
        JiraCloudTransitionHandler jiraTransitionHandler = new JiraCloudTransitionHandler(issueService);
        JiraCloudIssuePropertyHandler jiraIssuePropertyHandler = new JiraCloudIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraCustomFieldResolver jiraCloudCustomFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
        JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(gson);
        JiraCloudIssueHandler jiraIssueHandler = new JiraCloudIssueHandler(issueService, jiraProperties, jiraErrorMessageUtility, jiraTransitionHandler, jiraIssuePropertyHandler, contentValidator, jiraCloudCustomFieldResolver);
        return jiraIssueHandler.createOrUpdateIssues(validIssueConfig, requests);
    }

    private void checkIfAlertPluginIsInstalled(PluginManagerService jiraAppService) throws IssueTrackerException {
        logger.debug("Verifying the required application is installed on the Jira Cloud server...");
        try {
            boolean missingApp = !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
            if (missingApp) {
                throw new IssueTrackerException("Please configure the Jira Cloud plugin for your server instance via the global Jira Cloud channel settings.");
            }
        } catch (IntegrationException ex) {
            throw new IssueTrackerException("Please configure the Jira Cloud plugin for your server instance via the global Jira Cloud channel settings.", ex);
        }
    }

}
