/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.event;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionEventHandler;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsMessageSenderFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreator;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureBoardsTransitionEventHandler implements IssueTrackerTransitionEventHandler<AzureBoardsTransitionEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory;
    private final ProxyManager proxyManager;
    private final JobDetailsAccessor<AzureBoardsJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public AzureBoardsTransitionEventHandler(
        Gson gson,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory,
        ProxyManager proxyManager,
        JobDetailsAccessor<AzureBoardsJobDetailsModel> jobDetailsAccessor
    ) {
        this.gson = gson;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureBoardsMessageSenderFactory = azureBoardsMessageSenderFactory;
        this.proxyManager = proxyManager;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public void handle(AzureBoardsTransitionEvent event) {
        UUID jobId = event.getJobId();
        Optional<AzureBoardsJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                AzureBoardsJobDetailsModel distributionDetails = details.get();
                AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.createAzureBoardsProperties();
                String organizationName = azureBoardsProperties.getOrganizationName();
                azureBoardsProperties.validateProperties();

                // Initialize Http Service
                ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
                AzureHttpRequestCreator azureHttpRequestCreator = azureBoardsProperties.createAzureHttpRequestCreator(proxy, gson);
                AzureHttpService azureHttpService = new AzureHttpService(gson, azureHttpRequestCreator);

                // Common Azure Boards Services
                AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
                AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService, azureHttpRequestCreator);

                // Message Sender Requirements
                AzureWorkItemTypeStateService workItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService, apiVersionAppender);
                AzureWorkItemCommentService workItemCommentService = new AzureWorkItemCommentService(azureHttpService, apiVersionAppender);

                IssueTrackerMessageSender<Integer> messageSender = azureBoardsMessageSenderFactory.createMessageSender(
                    workItemService,
                    workItemTypeStateService,
                    workItemCommentService,
                    organizationName,
                    distributionDetails
                );
                IssueTransitionModel<Integer> transitionModel = event.getTransitionModel();
                messageSender.sendMessage(transitionModel);
            } catch (AlertException ex) {
                logger.error("Cannot transition issue for job {}", jobId);
                logger.error("Cause: ", ex);
            }
        } else {
            logger.error("No Azure Boards job found with id {}", jobId);
        }
    }
}
