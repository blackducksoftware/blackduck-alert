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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerIssueHandler;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerIssuePropertyHandler;
import com.synopsys.integration.alert.channel.jira.server.util.JiraServerTransitionHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerChannel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageResult;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
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

@Component
public class JiraServerChannel extends IssueTrackerChannel {
    private static final Logger logger = LoggerFactory.getLogger(JiraServerChannel.class);
    private final JiraMessageParser jiraMessageParser;
    private final JiraServerChannelKey descriptorKey;

    @Autowired
    public JiraServerChannel(Gson gson, AuditUtility auditUtility, JiraServerChannelKey descriptorKey, JiraMessageParser jiraMessageParser) {
        super(gson, auditUtility);
        this.descriptorKey = descriptorKey;
        this.jiraMessageParser = jiraMessageParser;
    }

    @Override
    public IssueTrackerMessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        FieldAccessor fieldAccessor = event.getFieldAccessor();
        MessageContentGroup content = event.getContent();
        JiraServerProperties jiraProperties = new JiraServerProperties(fieldAccessor);
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
        IssueConfig jiraIssueConfig = jiraIssueConfigValidator.validate(fieldAccessor);

        IssueService issueService = jiraServerServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        JiraServerTransitionHandler jiraTransitionHandler = new JiraServerTransitionHandler(issueService);
        JiraServerIssuePropertyHandler jiraIssuePropertyHandler = new JiraServerIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraServerIssueHandler jiraIssueHandler = new JiraServerIssueHandler(issueService, jiraProperties, jiraMessageParser, getGson(), jiraTransitionHandler, jiraIssuePropertyHandler);
        return jiraIssueHandler.createOrUpdateIssues(jiraIssueConfig, content);
    }

    @Override
    public String getDestinationName() {
        return descriptorKey.getUniversalKey();
    }

}
