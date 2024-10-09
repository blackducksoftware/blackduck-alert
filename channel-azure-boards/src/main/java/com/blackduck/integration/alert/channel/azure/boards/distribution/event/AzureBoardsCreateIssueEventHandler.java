/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.blackduck.integration.alert.channel.azure.boards.distribution.AzureBoardsMessageSenderFactory;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;
import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEventHandler;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.azure.boards.common.http.AzureApiVersionAppender;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreator;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpService;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.blackduck.integration.alert.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.blackduck.integration.alert.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.AzureWorkItemService;
import com.blackduck.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;

@Component
public class AzureBoardsCreateIssueEventHandler extends IssueTrackerCreateIssueEventHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory;
    private final ProxyManager proxyManager;
    private final JobDetailsAccessor<AzureBoardsJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public AzureBoardsCreateIssueEventHandler(
        EventManager eventManager,
        Gson gson,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory,
        ProxyManager proxyManager,
        JobDetailsAccessor<AzureBoardsJobDetailsModel> jobDetailsAccessor,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, responsePostProcessor, executingJobManager);
        this.gson = gson;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureBoardsMessageSenderFactory = azureBoardsMessageSenderFactory;
        this.proxyManager = proxyManager;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public synchronized void handleEvent(IssueTrackerCreateIssueEvent event) {
        UUID jobId = event.getJobId();
        Optional<AzureBoardsJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                AzureBoardsJobDetailsModel distributionDetails = details.get();
                AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.createAzureBoardsPropertiesWithJobId(jobId);
                String organizationName = azureBoardsProperties.getOrganizationName();
                azureBoardsProperties.validateProperties();

                // Initialize Http Service
                ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
                AzureHttpRequestCreator azureHttpRequestCreator = azureBoardsProperties.createAzureHttpRequestCreator(proxy, gson);
                AzureHttpService azureHttpService = new AzureHttpService(gson, azureHttpRequestCreator);

                // Common Azure Boards Services
                AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
                AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService, azureHttpRequestCreator);
                AzureWorkItemQueryService workItemQueryService = new AzureWorkItemQueryService(azureHttpService, apiVersionAppender);

                String projectNameOrId = distributionDetails.getProjectNameOrId();

                // Message Sender Requirements
                AzureWorkItemTypeStateService workItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService, apiVersionAppender);
                AzureWorkItemCommentService workItemCommentService = new AzureWorkItemCommentService(azureHttpService, apiVersionAppender);

                IssueTrackerMessageSender<Integer> messageSender = azureBoardsMessageSenderFactory.createMessageSender(
                    workItemService,
                    workItemTypeStateService,
                    workItemCommentService,
                    organizationName,
                    distributionDetails,
                    workItemQueryService
                );
                IssueCreationModel creationModel = event.getCreationModel();
                String query = creationModel.getQueryString().orElse(null);
                boolean issueDoesNotExist = checkIfIssueDoesNotExist(workItemQueryService, organizationName, projectNameOrId, query);
                if (issueDoesNotExist) {
                    List<IssueTrackerIssueResponseModel<Integer>> responses = messageSender.sendMessage(creationModel);
                    postProcess(new IssueTrackerResponse<>("Success", responses));
                    List<Integer> issueKeys = responses.stream()
                        .map(IssueTrackerIssueResponseModel::getIssueId)
                        .collect(Collectors.toList());
                    logger.info("Created issues: {}", issueKeys);
                }
            } catch (AlertException ex) {
                logger.error("Cannot create issue for job {}", jobId);
                logger.error("Query: {}", event.getCreationModel().getQueryString());
                logger.error("Cause: ", ex);
            }
        } else {
            logger.error("No Azure Boards job found with id {}", jobId);
        }
    }

    private boolean checkIfIssueDoesNotExist(AzureWorkItemQueryService queryService, String organizationName, String projectIdOrName, String query) {
        if (StringUtils.isBlank(query)) {
            return true;
        }

        try {
            return queryService.queryForWorkItems(organizationName, projectIdOrName, query).getWorkItems().isEmpty();
        } catch (HttpServiceException ex) {
            logger.error("Query executed: {}", query);
            logger.error("Couldn't execute query to see if issue exists.", ex);
        }
        return true;
    }
}


