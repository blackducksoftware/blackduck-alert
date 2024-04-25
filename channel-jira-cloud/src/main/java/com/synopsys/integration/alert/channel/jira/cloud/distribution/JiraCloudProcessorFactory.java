/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.certificates.AlertSSLContextManager;
import com.synopsys.integration.alert.api.channel.issue.tracker.IssueTrackerModelExtractor;
import com.synopsys.integration.alert.api.channel.issue.tracker.IssueTrackerProcessor;
import com.synopsys.integration.alert.api.channel.issue.tracker.IssueTrackerProcessorFactory;
import com.synopsys.integration.alert.api.channel.issue.tracker.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraMessageFormatter;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueStatusCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherFactory;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.api.descriptor.JiraCloudChannelKey;
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
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ProxyManager proxyManager;
    private final JiraCloudMessageSenderFactory messageSenderFactory;
    private final ProjectMessageToIssueModelTransformer modelTransformer;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final AlertSSLContextManager alertSSLContextManager;

    @Autowired
    public JiraCloudProcessorFactory(
        Gson gson,
        JiraMessageFormatter jiraMessageFormatter,
        JiraCloudChannelKey jiraCloudChannelKey,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        ProxyManager proxyManager,
        JiraCloudMessageSenderFactory messageSenderFactory,
        ProjectMessageToIssueModelTransformer modelTransformer,
        IssueCategoryRetriever issueCategoryRetriever,
        AlertSSLContextManager alertSSLContextManager
    ) {
        this.gson = gson;
        this.jiraMessageFormatter = jiraMessageFormatter;
        this.jiraCloudChannelKey = jiraCloudChannelKey;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.proxyManager = proxyManager;
        this.messageSenderFactory = messageSenderFactory;
        this.modelTransformer = modelTransformer;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.alertSSLContextManager = alertSSLContextManager;
    }

    @Override
    public IssueTrackerProcessor<String> createProcessor(JiraCloudJobDetailsModel distributionDetails, UUID jobExecutionId, Set<Long> notificationIds)
        throws AlertException {
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
        JiraIssueStatusCreator jiraIssueStatusCreator = new JiraIssueStatusCreator(distributionDetails.getResolveTransition(), distributionDetails.getReopenTransition());
        JiraSearcherFactory jiraSearcherFactory = new JiraSearcherFactory(
            issuePropertiesManager,
            jiraIssueStatusCreator,
            issueService::getTransitions,
            issueCategoryRetriever,
            modelTransformer
        );
        JiraCloudQueryExecutor jiraCloudQueryExecutor = new JiraCloudQueryExecutor(issueSearchService);
        IssueTrackerSearcher<String> jiraSearcher = jiraSearcherFactory.createJiraSearcher(distributionDetails.getProjectNameOrKey(), jiraCloudQueryExecutor);

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(jiraMessageFormatter, jiraSearcher);

        IssueTrackerAsyncMessageSender<String> messageSender = messageSenderFactory.createAsyncMessageSender(
            distributionDetails,
            jobExecutionId,
            notificationIds
        );

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private JiraCloudProperties createJiraCloudProperties() throws AlertConfigurationException {
        ConfigurationModel jiraCloudGlobalConfig = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(jiraCloudChannelKey, ConfigContextEnum.GLOBAL)
            .stream()
            .findAny()
            .orElseThrow(() -> new AlertConfigurationException("Missing Jira Cloud global configuration"));
        String jiraUrl = jiraCloudGlobalConfig.getField(JiraCloudDescriptor.KEY_JIRA_URL).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");

        return JiraCloudProperties.fromConfig(
            jiraCloudGlobalConfig,
            proxyManager.createProxyInfoForHost(jiraUrl),
            alertSSLContextManager.buildWithClientCertificate().orElse(null)
        );
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
