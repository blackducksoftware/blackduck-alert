/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCommentEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSenderFactory;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.azure.boards.common.http.AzureApiVersionAppender;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreator;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpService;
import com.blackduck.integration.alert.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.blackduck.integration.alert.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.blackduck.integration.alert.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.AzureWorkItemService;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsHttpExceptionMessageImprover;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.blackduck.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsCommentGenerator;
import com.blackduck.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsCreateEventGenerator;
import com.blackduck.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueCommenter;
import com.blackduck.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueCreator;
import com.blackduck.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsIssueTransitioner;
import com.blackduck.integration.alert.channel.azure.boards.distribution.delegate.AzureBoardsTransitionGenerator;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsAlertIssuePropertiesManager;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

@Component
public class AzureBoardsMessageSenderFactory implements IssueTrackerMessageSenderFactory<AzureBoardsJobDetailsModel, Integer, IssueTrackerModelHolder<Integer>> {
    private final Gson gson;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final AzureBoardsChannelKey channelKey;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final ProxyManager proxyManager;
    private final AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final EventManager eventManager;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public AzureBoardsMessageSenderFactory(
        Gson gson,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        AzureBoardsChannelKey channelKey,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        ProxyManager proxyManager,
        AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover,
        IssueCategoryRetriever issueCategoryRetriever,
        EventManager eventManager,
        ExecutingJobManager executingJobManager
    ) {
        this.gson = gson;
        this.callbackInfoCreator = callbackInfoCreator;
        this.channelKey = channelKey;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.proxyManager = proxyManager;
        this.exceptionMessageImprover = exceptionMessageImprover;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.eventManager = eventManager;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public IssueTrackerMessageSender<Integer> createMessageSender(AzureBoardsJobDetailsModel distributionDetails, UUID globalId) throws AlertException {
        AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.createAzureBoardsProperties(globalId);
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
        AzureWorkItemQueryService workItemQueryService = new AzureWorkItemQueryService(azureHttpService, apiVersionAppender);

        return createMessageSender(
            workItemService,
            workItemTypeStateService,
            workItemCommentService,
            azureBoardsProperties.getOrganizationName(),
            distributionDetails,
            workItemQueryService
        );
    }

    @Override
    public IssueTrackerAsyncMessageSender<Integer> createAsyncMessageSender(
        AzureBoardsJobDetailsModel distributionDetails, UUID globalId,
        UUID jobExecutionId,
        Set<Long> notificationIds
    ) throws AlertException {
        return createAsyncMessageSender(distributionDetails, jobExecutionId, notificationIds);
    }

    public IssueTrackerMessageSender<Integer> createMessageSender(
        AzureWorkItemService workItemService,
        AzureWorkItemTypeStateService workItemTypeStateService,
        AzureWorkItemCommentService workItemCommentService,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemQueryService workItemQueryService
    ) {
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);
        AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever = new AzureBoardsWorkItemTypeStateRetriever(gson, workItemService, workItemTypeStateService);
        AzureBoardsAlertIssuePropertiesManager issuePropertiesManager = new AzureBoardsAlertIssuePropertiesManager();

        // Message Sender Requirements
        AzureBoardsIssueCommenter commenter = new AzureBoardsIssueCommenter(issueResponseCreator, organizationName, distributionDetails, workItemCommentService);
        AzureBoardsIssueTransitioner transitioner = new AzureBoardsIssueTransitioner(
            commenter,
            issueResponseCreator,
            gson,
            organizationName,
            distributionDetails,
            workItemService,
            workItemTypeStateRetriever,
            exceptionMessageImprover
        );
        AzureBoardsIssueCreator creator = new AzureBoardsIssueCreator(
            channelKey,
            commenter,
            callbackInfoCreator,
            gson,
            organizationName,
            distributionDetails,
            workItemService,
            issuePropertiesManager,
            exceptionMessageImprover,
            issueCategoryRetriever,
            workItemQueryService
        );

        return new IssueTrackerMessageSender<>(
            creator,
            transitioner,
            commenter
        );
    }

    public IssueTrackerAsyncMessageSender<Integer> createAsyncMessageSender(
        AzureBoardsJobDetailsModel distributionDetails,
        UUID jobExecutionId,
        Set<Long> notificationIds
    ) {
        UUID jobId = distributionDetails.getJobId();
        IssueTrackerCommentEventGenerator<Integer> commentEventGenerator = new AzureBoardsCommentGenerator(channelKey, jobExecutionId, jobId, notificationIds);
        IssueTrackerCreationEventGenerator createEventGenerator = new AzureBoardsCreateEventGenerator(channelKey, jobExecutionId, jobId, notificationIds);
        IssueTrackerTransitionEventGenerator<Integer> transitionEventGenerator = new AzureBoardsTransitionGenerator(
            channelKey,
            jobExecutionId,
            jobId,
            notificationIds
        );

        return new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            eventManager,
            jobExecutionId,
            notificationIds,
            executingJobManager
        );
    }

}
