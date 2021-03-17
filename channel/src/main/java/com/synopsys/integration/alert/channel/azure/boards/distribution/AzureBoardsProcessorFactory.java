/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerModelExtractor;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerProcessor;
import com.synopsys.integration.alert.channel.api.issue.IssueTrackerProcessorFactory;
import com.synopsys.integration.alert.channel.api.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueCommenter;
import com.synopsys.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueCreator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueTransitioner;
import com.synopsys.integration.alert.channel.azure.boards.distribution.search.AzureBoardsAlertIssuePropertiesManager;
import com.synopsys.integration.alert.channel.azure.boards.distribution.search.AzureBoardsSearcher;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.synopsys.integration.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;

@Component
public class AzureBoardsProcessorFactory implements IssueTrackerProcessorFactory<AzureBoardsJobDetailsModel, Integer> {
    private final Gson gson;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final AzureBoardsChannelKey channelKey;
    private final AzureBoardsMessageFormatter formatter;
    private final AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final ConfigurationAccessor configurationAccessor;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsProcessorFactory(
        Gson gson,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        AzureBoardsChannelKey channelKey,
        AzureBoardsMessageFormatter formatter,
        AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory,
        AzureRedirectUrlCreator azureRedirectUrlCreator,
        ConfigurationAccessor configurationAccessor,
        ProxyManager proxyManager
    ) {
        this.gson = gson;
        this.callbackInfoCreator = callbackInfoCreator;
        this.channelKey = channelKey;
        this.formatter = formatter;
        this.credentialDataStoreFactory = credentialDataStoreFactory;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.configurationAccessor = configurationAccessor;
        this.proxyManager = proxyManager;
    }

    @Override
    public IssueTrackerProcessor<Integer> createProcessor(AzureBoardsJobDetailsModel distributionDetails) throws AlertException {
        AzureBoardsProperties azureBoardsProperties = createAzureBoardsProperties();
        String organizationName = azureBoardsProperties.getOrganizationName();
        azureBoardsProperties.validateProperties();

        // Initialize Http Service
        HttpTransport httpTransport = azureBoardsProperties.createHttpTransport(proxyManager.createProxyInfo());
        Credential oAuthCredential = retrieveOAuthCredential(azureBoardsProperties, httpTransport);
        AzureHttpService azureHttpService = AzureHttpServiceFactory.withCredential(httpTransport, oAuthCredential, gson);

        // Azure Boards Services
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
        AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService);
        AzureWorkItemTypeStateService workItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService, apiVersionAppender);
        AzureWorkItemQueryService workItemQueryService = new AzureWorkItemQueryService(azureHttpService, apiVersionAppender);
        AzureWorkItemCommentService workItemCommentService = new AzureWorkItemCommentService(azureHttpService, apiVersionAppender);

        // Helper Classes
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);
        AzureBoardsIssueTrackerQueryManager queryManager = new AzureBoardsIssueTrackerQueryManager(organizationName, distributionDetails, workItemService, workItemQueryService);
        AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever = new AzureBoardsWorkItemTypeStateRetriever(gson, workItemService, workItemTypeStateService);
        AzureBoardsAlertIssuePropertiesManager issuePropertiesManager = new AzureBoardsAlertIssuePropertiesManager();

        // Message Sender Requirements
        AzureBoardsIssueCommenter commenter = new AzureBoardsIssueCommenter(issueResponseCreator, organizationName, distributionDetails, workItemCommentService);
        AzureBoardsIssueTransitioner transitioner = new AzureBoardsIssueTransitioner(commenter, issueResponseCreator, gson, organizationName, distributionDetails, workItemService, workItemTypeStateRetriever);
        AzureBoardsIssueCreator creator = new AzureBoardsIssueCreator(channelKey, commenter, callbackInfoCreator, gson, organizationName, distributionDetails, workItemService, issuePropertiesManager);

        // Extractor Requirement
        AzureBoardsSearcher azureBoardsSearcher = new AzureBoardsSearcher(gson, organizationName, queryManager);

        IssueTrackerModelExtractor<Integer> extractor = new IssueTrackerModelExtractor<>(formatter, azureBoardsSearcher);
        IssueTrackerMessageSender<Integer> messageSender = new IssueTrackerMessageSender<>(creator, transitioner, commenter);

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private AzureBoardsProperties createAzureBoardsProperties() throws AlertConfigurationException {
        ConfigurationModel azureBoardsGlobalConfiguration = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(channelKey, ConfigContextEnum.GLOBAL)
                                                                .stream()
                                                                .findAny()
                                                                .orElseThrow(() -> new AlertConfigurationException("Missing AzureBoards global configuration"));
        return AzureBoardsProperties.fromGlobalConfig(credentialDataStoreFactory, azureRedirectUrlCreator.createOAuthRedirectUri(), azureBoardsGlobalConfiguration);
    }

    private Credential retrieveOAuthCredential(AzureBoardsProperties azureBoardsProperties, HttpTransport httpTransport) throws AlertException {
        try {
            AuthorizationCodeFlow oAuthFlow = azureBoardsProperties.createOAuthFlow(httpTransport);
            return azureBoardsProperties.getExistingOAuthCredential(oAuthFlow)
                       .orElseThrow(() -> new AlertConfigurationException("No stored OAuth credential for Azure Boards exists"));
        } catch (IOException e) {
            throw new AlertException("Cannot initialize OAuth for Azure Boards", e);
        }
    }

}
