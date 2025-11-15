/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerModelExtractor;
import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerProcessorFactory;
import com.blackduck.integration.alert.api.channel.issue.tracker.convert.ProjectMessageToIssueModelTransformer;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraMessageFormatter;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueStatusCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherFactory;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.rest.service.PluginManagerService;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.google.gson.Gson;

@Component
public class JiraServerProcessorFactory implements IssueTrackerProcessorFactory<JiraServerJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraMessageFormatter jiraMessageFormatter;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerMessageSenderFactory jiraServerMessageSenderFactory;
    private final ProjectMessageToIssueModelTransformer modelTransformer;
    private final IssueCategoryRetriever issueCategoryRetriever;

    @Autowired
    public JiraServerProcessorFactory(
        Gson gson,
        JiraMessageFormatter jiraMessageFormatter,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory,
        ProjectMessageToIssueModelTransformer modelTransformer,
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        this.gson = gson;
        this.jiraMessageFormatter = jiraMessageFormatter;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerMessageSenderFactory = jiraServerMessageSenderFactory;
        this.modelTransformer = modelTransformer;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    @Override
    public IssueTrackerProcessor<String> createProcessor(JiraServerJobDetailsModel distributionDetails, UUID jobExecutionId, Set<Long> notificationIds)
        throws AlertException {
        JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraPropertiesWithJobId(distributionDetails.getJobId());
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
        JiraIssueStatusCreator jiraIssueStatusCreator = new JiraIssueStatusCreator(distributionDetails.getResolveTransition(), distributionDetails.getReopenTransition());
        JiraSearcherFactory jiraSearcherFactory = new JiraSearcherFactory(
            issuePropertiesManager,
            jiraIssueStatusCreator,
            issueService::getTransitions,
            issueCategoryRetriever,
            modelTransformer
        );
        JiraServerQueryExecutor jiraServerQueryExecutor = new JiraServerQueryExecutor(issueSearchService);
        IssueTrackerSearcher<String> jiraSearcher = jiraSearcherFactory.createJiraSearcher(distributionDetails.getProjectNameOrKey(), jiraServerQueryExecutor);

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(jiraMessageFormatter, jiraSearcher);

        IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> messageSender = jiraServerMessageSenderFactory.createAsyncMessageSender(
            distributionDetails,
            jobExecutionId,
            notificationIds
        );

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
