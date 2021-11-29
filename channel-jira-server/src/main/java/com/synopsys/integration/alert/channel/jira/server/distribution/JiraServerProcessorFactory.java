/*
 * channel-jira-server
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerModelExtractor;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerProcessor;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerProcessorFactory;
import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraMessageFormatter;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;

@Component
public class JiraServerProcessorFactory implements IssueTrackerProcessorFactory<JiraServerJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraMessageFormatter jiraMessageFormatter;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerMessageSenderFactory jiraServerMessageSenderFactory;
    private final ProjectMessageToIssueModelTransformer modelTransformer;

    @Autowired
    public JiraServerProcessorFactory(
        Gson gson,
        JiraMessageFormatter jiraMessageFormatter,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory,
        ProjectMessageToIssueModelTransformer modelTransformer
    ) {
        this.gson = gson;
        this.jiraMessageFormatter = jiraMessageFormatter;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerMessageSenderFactory = jiraServerMessageSenderFactory;
        this.modelTransformer = modelTransformer;
    }

    @Override
    public IssueTrackerProcessor<String> createProcessor(JiraServerJobDetailsModel distributionDetails) throws AlertException {
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties();
        JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);

        if (!jiraProperties.isPluginCheckDisabled()) {
            checkIfAlertPluginIsInstalled(jiraServerServiceFactory.createPluginManagerService());
        }

        // Jira Services
        IssueService issueService = jiraServerServiceFactory.createIssueService();
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
        IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();

        // Common Helpers
        JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);

        // Extractor Requirement
        JiraServerSearcher jiraServerSearcher = new JiraServerSearcher(distributionDetails.getProjectNameOrKey(), issueSearchService, issuePropertiesManager, modelTransformer);

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(jiraMessageFormatter, jiraServerSearcher);
        IssueTrackerMessageSender<String> messageSender = jiraServerMessageSenderFactory.createMessageSender(issueService, distributionDetails, jiraServerServiceFactory, issuePropertiesManager);

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private void checkIfAlertPluginIsInstalled(PluginManagerService jiraAppService) throws IssueTrackerException {
        logger.debug("Verifying the required application is installed on the Jira Server server...");
        try {
            boolean missingApp = !jiraAppService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
            if (missingApp) {
                throw new IssueTrackerException("Please configure the Jira Server plugin for your server instance via the global Jira Server channel settings.");
            }
        } catch (IntegrationException ex) {
            throw new IssueTrackerException("Please configure the Jira Server plugin for your server instance via the global Jira Server channel settings.", ex);
        }
    }

}
