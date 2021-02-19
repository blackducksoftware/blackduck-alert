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
package com.synopsys.integration.alert.channel.jira2.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira2.cloud.delegate.JiraCloudIssueCommentCreator;
import com.synopsys.integration.alert.channel.jira2.cloud.delegate.JiraCloudIssueCreator;
import com.synopsys.integration.alert.channel.jira2.cloud.delegate.JiraCloudIssueTransitioner;
import com.synopsys.integration.alert.channel.jira2.common.JiraCustomFieldResolver;
import com.synopsys.integration.alert.channel.jira2.common.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira2.common.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.channel.jira2.common.JiraMessageFormatter;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.FieldService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopys.integration.alert.channel.api.issue.AlertIssueOriginCreator;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerIssueResponseCreator;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerMessageSender;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerModelExtractor;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerProcessor;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerProcessorFactory;

@Component
public class JiraCloudProcessorFactory implements IssueTrackerProcessorFactory<JiraCloudJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraMessageFormatter jiraMessageFormatter;
    private final JiraCloudChannelKey jiraCloudChannelKey;
    private final JiraErrorMessageUtility jiraErrorMessageUtility;
    private final ConfigurationAccessor configurationAccessor;
    private final ProxyManager proxyManager;

    @Autowired
    public JiraCloudProcessorFactory(
        Gson gson,
        JiraMessageFormatter jiraMessageFormatter,
        JiraCloudChannelKey jiraCloudChannelKey,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        ConfigurationAccessor configurationAccessor,
        ProxyManager proxyManager
    ) {
        this.gson = gson;
        this.jiraMessageFormatter = jiraMessageFormatter;
        this.jiraCloudChannelKey = jiraCloudChannelKey;
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
        this.configurationAccessor = configurationAccessor;
        this.proxyManager = proxyManager;
    }

    @Override
    public IssueTrackerProcessor<String> createProcessor(JiraCloudJobDetailsModel distributionDetails) throws AlertException {
        JiraCloudProperties jiraProperties = createJiraCloudProperties();
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);

        if (!jiraProperties.isPluginCheckDisabled()) {
            checkIfAlertPluginIsInstalled(jiraCloudServiceFactory.createPluginManagerService());
        }

        JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, jiraCloudServiceFactory.createIssuePropertyService());
        JiraCloudSearcher jiraCloudSearcher = new JiraCloudSearcher(distributionDetails.getProjectNameOrKey(), jiraCloudServiceFactory.createIssueSearchService(), issuePropertiesManager);

        FieldService fieldService = jiraCloudServiceFactory.createFieldService();
        JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
        JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver);

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(jiraMessageFormatter, jiraCloudSearcher);

        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        ProjectService projectService = jiraCloudServiceFactory.createProjectService();

        AlertIssueOriginCreator alertIssueOriginCreator = new AlertIssueOriginCreator();
        IssueTrackerIssueResponseCreator<String> issueResponseCreator = new IssueTrackerIssueResponseCreator<>(alertIssueOriginCreator);

        JiraCloudIssueCommentCreator issueCommentCreator = new JiraCloudIssueCommentCreator(issueResponseCreator, distributionDetails, issueService);
        JiraCloudIssueTransitioner issueTransitioner = new JiraCloudIssueTransitioner(distributionDetails, issueService, issueResponseCreator, issueCommentCreator);
        JiraCloudIssueCreator issueCreator = new JiraCloudIssueCreator(
            distributionDetails,
            issueService,
            projectService,
            issueCreationRequestCreator,
            issueCommentCreator,
            issuePropertiesManager,
            jiraErrorMessageUtility,
            alertIssueOriginCreator
        );
        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(issueCreator, issueTransitioner, issueCommentCreator);

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private JiraCloudProperties createJiraCloudProperties() throws AlertConfigurationException {
        ConfigurationModel jiraCloudGlobalConfig = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(jiraCloudChannelKey, ConfigContextEnum.GLOBAL)
                                                       .stream()
                                                       .findAny()
                                                       .orElseThrow(() -> new AlertConfigurationException("Missing Jira Cloud global configuration"));
        return JiraCloudProperties.fromConfig(jiraCloudGlobalConfig, proxyManager.createProxyInfo());
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
