/*
 * channel
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
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerModelExtractor;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerProcessor;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerProcessorFactory;
import com.synopsys.integration.alert.channel.api.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira.common.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.channel.jira.common.distribution.JiraMessageFormatter;
import com.synopsys.integration.alert.channel.jira.common.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.channel.jira.common.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCommenter;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCreator;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueTransitioner;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;

@Component
public class JiraServerProcessorFactory implements IssueTrackerProcessorFactory<JiraServerJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraMessageFormatter jiraMessageFormatter;
    private final JiraServerChannelKey jiraServerChannelKey;
    private final JiraErrorMessageUtility jiraErrorMessageUtility;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final ConfigurationAccessor configurationAccessor;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;

    @Autowired
    public JiraServerProcessorFactory(
        Gson gson,
        JiraMessageFormatter jiraMessageFormatter,
        JiraServerChannelKey jiraServerChannelKey,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        ConfigurationAccessor configurationAccessor,
        JiraServerPropertiesFactory jiraServerPropertiesFactory
    ) {
        this.gson = gson;
        this.jiraMessageFormatter = jiraMessageFormatter;
        this.jiraServerChannelKey = jiraServerChannelKey;
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
        this.callbackInfoCreator = callbackInfoCreator;
        this.configurationAccessor = configurationAccessor;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
    }

    @Override
    public IssueTrackerProcessor<String> createProcessor(JiraServerJobDetailsModel distributionDetails) throws AlertException {
        JiraServerProperties jiraProperties = createJiraServerProperties();
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
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);

        // Message Sender Requirements
        JiraServerIssueCommenter issueCommenter = new JiraServerIssueCommenter(issueResponseCreator, issueService, distributionDetails);
        JiraServerIssueTransitioner issueTransitioner = new JiraServerIssueTransitioner(issueCommenter, issueResponseCreator, distributionDetails, issueService);
        JiraServerIssueCreator issueCreator = createIssueCreator(distributionDetails, jiraServerServiceFactory, issuePropertiesManager, issueService, issueCommenter);

        // Extractor Requirement
        JiraServerSearcher jiraServerSearcher = new JiraServerSearcher(distributionDetails.getProjectNameOrKey(), issueSearchService, issuePropertiesManager);

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(jiraMessageFormatter, jiraServerSearcher);
        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(issueCreator, issueTransitioner, issueCommenter);

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private JiraServerIssueCreator createIssueCreator(
        JiraServerJobDetailsModel distributionDetails,
        JiraServerServiceFactory jiraServerServiceFactory,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        IssueService issueService,
        JiraServerIssueCommenter issueCommenter
    ) {
        ProjectService projectService = jiraServerServiceFactory.createProjectService();
        FieldService fieldService = jiraServerServiceFactory.createFieldService();

        JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
        JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver);

        return new JiraServerIssueCreator(
            jiraServerChannelKey,
            issueCommenter,
            callbackInfoCreator,
            distributionDetails,
            issueService,
            projectService,
            issueCreationRequestCreator,
            issuePropertiesManager,
            jiraErrorMessageUtility
        );
    }

    private JiraServerProperties createJiraServerProperties() throws AlertConfigurationException {
        ConfigurationModel jiraServerGlobalConfig = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(jiraServerChannelKey, ConfigContextEnum.GLOBAL)
                                                        .stream()
                                                        .findAny()
                                                        .orElseThrow(() -> new AlertConfigurationException("Missing Jira Server global configuration"));

        FieldUtility fieldUtility = new FieldUtility(jiraServerGlobalConfig.getCopyOfKeyToFieldMap());
        return jiraServerPropertiesFactory.createJiraProperties(fieldUtility);
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
