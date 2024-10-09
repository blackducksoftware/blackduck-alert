package com.blackduck.integration.alert.channel.azure.boards.distribution.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionEventHandler;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
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
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.blackduck.integration.alert.channel.azure.boards.distribution.AzureBoardsMessageSenderFactory;
import com.blackduck.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

@Component
public class AzureBoardsTransitionEventHandler extends IssueTrackerTransitionEventHandler<AzureBoardsTransitionEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory;
    private final ProxyManager proxyManager;
    private final JobDetailsAccessor<AzureBoardsJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public AzureBoardsTransitionEventHandler(
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
    public void handleEvent(AzureBoardsTransitionEvent event) throws AlertException {
        UUID jobId = event.getJobId();
        Optional<AzureBoardsJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
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
            IssueTransitionModel<Integer> transitionModel = event.getTransitionModel();
            List<IssueTrackerIssueResponseModel<Integer>> responses = messageSender.sendMessage(transitionModel);
            postProcess(new IssueTrackerResponse<>("Success", responses));
        } else {
            logger.error("No Azure Boards job found with id {}", jobId);
        }
    }
}
