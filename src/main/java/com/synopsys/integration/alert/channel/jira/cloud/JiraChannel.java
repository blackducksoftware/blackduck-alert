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
package com.synopsys.integration.alert.channel.jira.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudIssueHandler;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudIssuePropertyHandler;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerChannel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageResult;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.service.UserSearchService;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

@Component
public class JiraChannel extends IssueTrackerChannel {
    private final Logger logger = LoggerFactory.getLogger(JiraChannel.class);

    private final JiraChannelKey jiraChannelKey;
    private final JiraMessageParser jiraMessageParser;

    @Autowired
    public JiraChannel(JiraChannelKey jiraChannelKey, JiraMessageParser jiraMessageParser, Gson gson, AuditUtility auditUtility) {
        super(gson, auditUtility);
        this.jiraChannelKey = jiraChannelKey;
        this.jiraMessageParser = jiraMessageParser;
    }

    @Override
    public IssueTrackerMessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        FieldAccessor fieldAccessor = event.getFieldAccessor();
        MessageContentGroup content = event.getContent();
        JiraProperties jiraProperties = new JiraProperties(fieldAccessor);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, getGson());
        PluginManagerService jiraAppService = jiraCloudServiceFactory.createPluginManagerService();
        logger.debug("Verifying the required application is installed on the Jira Cloud server...");
        boolean missingApp = jiraAppService.getInstalledApp(jiraProperties.getUsername(), jiraProperties.getAccessToken(), JiraConstants.JIRA_APP_KEY).isEmpty();
        if (missingApp) {
            throw new AlertException("Please configure the Jira Cloud plugin for your server instance via the global Jira Cloud channel settings.");
        }

        ProjectService projectService = jiraCloudServiceFactory.createProjectService();
        UserSearchService userSearchService = jiraCloudServiceFactory.createUserSearchService();
        IssueTypeService issueTypeService = jiraCloudServiceFactory.createIssueTypeService();
        IssueMetaDataService issueMetaDataService = jiraCloudServiceFactory.createIssueMetadataService();

        JiraIssueConfigValidator jiraIssueConfigValidator = new JiraIssueConfigValidator(projectService, userSearchService, issueTypeService, issueMetaDataService);
        IssueConfig jiraIssueConfig = jiraIssueConfigValidator.validate(fieldAccessor);

        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
        JiraCloudTransitionHandler jiraTransitionHandler = new JiraCloudTransitionHandler(issueService);
        JiraCloudIssuePropertyHandler jiraIssuePropertyHandler = new JiraCloudIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraCloudIssueHandler jiraIssueHandler = new JiraCloudIssueHandler(issueService, jiraProperties, jiraMessageParser, getGson(), jiraTransitionHandler, jiraIssuePropertyHandler);
        return jiraIssueHandler.createOrUpdateIssues(jiraIssueConfig, content);
    }

    @Override
    public String getDestinationName() {
        return jiraChannelKey.getUniversalKey();
    }

}
