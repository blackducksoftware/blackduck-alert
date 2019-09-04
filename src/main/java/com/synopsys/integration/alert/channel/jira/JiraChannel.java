/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.JiraIssueConfigValidator.JiraIssueConfig;
import com.synopsys.integration.alert.channel.jira.model.JiraMessageResult;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraAppService;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.rest.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.rest.service.UserSearchService;

@Component
public class JiraChannel extends DistributionChannel {
    private final Logger logger = LoggerFactory.getLogger(JiraChannel.class);

    private final JiraChannelKey jiraChannelKey;

    @Autowired
    public JiraChannel(Gson gson, AuditUtility auditUtility, JiraChannelKey jiraChannelKey) {
        super(gson, auditUtility);
        this.jiraChannelKey = jiraChannelKey;
    }

    @Override
    public JiraMessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        FieldAccessor fieldAccessor = event.getFieldAccessor();
        MessageContentGroup content = event.getContent();
        JiraProperties jiraProperties = new JiraProperties(fieldAccessor);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, getGson());
        JiraAppService jiraAppService = jiraCloudServiceFactory.createJiraAppService();
        logger.debug("Verifying the required application is installed on the Jira Cloud server...");
        boolean missingApp = jiraAppService.getInstalledApp(jiraProperties.getUsername(), jiraProperties.getAccessToken(), JiraConstants.JIRA_APP_KEY).isEmpty();
        if (missingApp) {
            throw new AlertException("Please configure the Jira Cloud plugin for your server instance via the global Jira Cloud channel settings.");
        }

        ProjectService projectService = jiraCloudServiceFactory.createProjectService();
        UserSearchService userSearchService = jiraCloudServiceFactory.createUserSearchService();
        IssueTypeService issueTypeService = jiraCloudServiceFactory.createIssueTypeService();

        JiraIssueConfigValidator jiraIssueConfigValidator = new JiraIssueConfigValidator(projectService, userSearchService, issueTypeService);
        JiraIssueConfig jiraIssueConfig = jiraIssueConfigValidator.validate(fieldAccessor);

        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();

        JiraIssueHandler jiraIssueHandler = new JiraIssueHandler(issueService, issueSearchService, issuePropertyService, jiraProperties, getGson());
        return jiraIssueHandler.createOrUpdateIssues(jiraIssueConfig, content);
    }

    @Override
    public String getDestinationName() {
        return jiraChannelKey.getUniversalKey();
    }

}
