/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerModelExtractor;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerProcessor;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerProcessorFactory;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.distribution.JiraMessageFormatter;
import com.synopsys.integration.alert.channel.jira.common.distribution.search.JiraIssueAlertPropertiesManager;
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
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

@Component
public class JiraCloudProcessorFactory implements IssueTrackerProcessorFactory<JiraCloudJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraMessageFormatter jiraMessageFormatter;
    private final JiraCloudChannelKey jiraCloudChannelKey;
    private final ConfigurationAccessor configurationAccessor;
    private final ProxyManager proxyManager;
    private final JiraCloudMessageSenderFactory messageSenderFactory;

    @Autowired
    public JiraCloudProcessorFactory(
        Gson gson,
        JiraMessageFormatter jiraMessageFormatter,
        JiraCloudChannelKey jiraCloudChannelKey,
        ConfigurationAccessor configurationAccessor,
        ProxyManager proxyManager,
        JiraCloudMessageSenderFactory messageSenderFactory
    ) {
        this.gson = gson;
        this.jiraMessageFormatter = jiraMessageFormatter;
        this.jiraCloudChannelKey = jiraCloudChannelKey;
        this.configurationAccessor = configurationAccessor;
        this.proxyManager = proxyManager;
        this.messageSenderFactory = messageSenderFactory;
    }

    @Override
    public IssueTrackerProcessor<String> createProcessor(JiraCloudJobDetailsModel distributionDetails) throws AlertException {
        JiraCloudProperties jiraProperties = createJiraCloudProperties();
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);

        if (!jiraProperties.isPluginCheckDisabled()) {
            checkIfAlertPluginIsInstalled(jiraCloudServiceFactory.createPluginManagerService());
        }

        // Jira Cloud Services
        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
        IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();

        // Common Helpers
        JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);

        // Extractor Requirement
        JiraCloudSearcher jiraCloudSearcher = new JiraCloudSearcher(distributionDetails.getProjectNameOrKey(), issueSearchService, issuePropertiesManager);

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(jiraMessageFormatter, jiraCloudSearcher);
        IssueTrackerMessageSender<String> messageSender = messageSenderFactory.createMessageSender(issueService, distributionDetails, jiraCloudServiceFactory, issuePropertiesManager);

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
