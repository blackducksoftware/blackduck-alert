/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSenderFactory;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsHttpExceptionMessageImprover;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueCommenter;
import com.synopsys.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueCreator;
import com.synopsys.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueTransitioner;
import com.synopsys.integration.alert.channel.azure.boards.distribution.search.AzureBoardsAlertIssuePropertiesManager;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreator;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureBoardsMessageSenderFactory implements IssueTrackerMessageSenderFactory<AzureBoardsJobDetailsModel, Integer> {
    private final Gson gson;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final AzureBoardsChannelKey channelKey;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final ProxyManager proxyManager;
    private final AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover;
    private final IssueCategoryRetriever issueCategoryRetriever;

    @Autowired
    public AzureBoardsMessageSenderFactory(
        Gson gson,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        AzureBoardsChannelKey channelKey,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        ProxyManager proxyManager,
        AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover,
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        this.gson = gson;
        this.callbackInfoCreator = callbackInfoCreator;
        this.channelKey = channelKey;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.proxyManager = proxyManager;
        this.exceptionMessageImprover = exceptionMessageImprover;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    @Override
    public IssueTrackerMessageSender<Integer> createMessageSender(AzureBoardsJobDetailsModel distributionDetails) throws AlertException {
        AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.createAzureBoardsProperties();
        azureBoardsProperties.validateProperties();

        // Initialize Http Service
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
        AzureHttpRequestCreator azureHttpRequestCreator = azureBoardsProperties.createAzureHttpRequestCreator(proxy, gson);
        AzureHttpService azureHttpService = new AzureHttpService(gson, azureHttpRequestCreator);

        // Azure Boards Services
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
        AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService, azureHttpRequestCreator);
        AzureWorkItemTypeStateService workItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService, apiVersionAppender);
        AzureWorkItemCommentService workItemCommentService = new AzureWorkItemCommentService(azureHttpService, apiVersionAppender);

        return createMessageSender(
            workItemService,
            workItemTypeStateService,
            workItemCommentService,
            azureBoardsProperties.getOrganizationName(),
            distributionDetails
        );
    }

    public IssueTrackerMessageSender<Integer> createMessageSender(
        AzureWorkItemService workItemService,
        AzureWorkItemTypeStateService workItemTypeStateService,
        AzureWorkItemCommentService workItemCommentService,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails
    ) {
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);
        AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever = new AzureBoardsWorkItemTypeStateRetriever(gson, workItemService, workItemTypeStateService);
        AzureBoardsAlertIssuePropertiesManager issuePropertiesManager = new AzureBoardsAlertIssuePropertiesManager();

        // Message Sender Requirements
        AzureBoardsIssueCommenter commenter = new AzureBoardsIssueCommenter(issueResponseCreator, organizationName, distributionDetails, workItemCommentService);
        AzureBoardsIssueTransitioner transitioner = new AzureBoardsIssueTransitioner(commenter, issueResponseCreator, gson, organizationName, distributionDetails, workItemService, workItemTypeStateRetriever, exceptionMessageImprover);
        AzureBoardsIssueCreator creator = new AzureBoardsIssueCreator(channelKey, commenter, callbackInfoCreator, gson, organizationName, distributionDetails, workItemService, issuePropertiesManager, exceptionMessageImprover,
            issueCategoryRetriever);

        return new IssueTrackerMessageSender<>(creator, transitioner, commenter);
    }

}
