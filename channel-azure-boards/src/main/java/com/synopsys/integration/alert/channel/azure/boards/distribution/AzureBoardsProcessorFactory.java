/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerModelExtractor;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerProcessor;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerProcessorFactory;
import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.distribution.search.AzureBoardsSearcher;
import com.synopsys.integration.alert.channel.azure.boards.distribution.search.AzureCustomFieldManager;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.synopsys.integration.azure.boards.common.service.process.AzureProcessService;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureBoardsProcessorFactory implements IssueTrackerProcessorFactory<AzureBoardsJobDetailsModel, Integer> {
    private final Gson gson;
    private final AzureBoardsMessageFormatter formatter;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory;
    private final ProxyManager proxyManager;
    private final ProjectMessageToIssueModelTransformer modelTransformer;

    @Autowired
    public AzureBoardsProcessorFactory(
        Gson gson,
        AzureBoardsMessageFormatter formatter,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory,
        ProxyManager proxyManager,
        ProjectMessageToIssueModelTransformer modelTransformer
    ) {
        this.gson = gson;
        this.formatter = formatter;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureBoardsMessageSenderFactory = azureBoardsMessageSenderFactory;
        this.proxyManager = proxyManager;
        this.modelTransformer = modelTransformer;
    }

    @Override
    public IssueTrackerProcessor<Integer> createProcessor(AzureBoardsJobDetailsModel distributionDetails) throws AlertException {
        AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.createAzureBoardsProperties();
        String organizationName = azureBoardsProperties.getOrganizationName();
        azureBoardsProperties.validateProperties();

        // Initialize Http Service
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpServiceFactory.DEFAULT_BASE_URL);
        AzureHttpService azureHttpService = azureBoardsProperties.createAzureHttpService(proxy, gson);

        // Common Azure Boards Services
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
        AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService);
        AzureWorkItemQueryService workItemQueryService = new AzureWorkItemQueryService(azureHttpService, apiVersionAppender);

        installCustomFieldsIfNecessary(
            organizationName,
            distributionDetails.getProjectNameOrId(),
            distributionDetails.getWorkItemType(),
            new AzureProjectService(azureHttpService, apiVersionAppender),
            new AzureProcessService(azureHttpService, apiVersionAppender)
        );

        // Extractor Requirements
        AzureBoardsIssueTrackerQueryManager queryManager = new AzureBoardsIssueTrackerQueryManager(organizationName, distributionDetails, workItemService, workItemQueryService);
        AzureBoardsSearcher azureBoardsSearcher = new AzureBoardsSearcher(gson, organizationName, queryManager, modelTransformer);

        IssueTrackerModelExtractor<Integer> extractor = new IssueTrackerModelExtractor<>(formatter, azureBoardsSearcher);

        // Message Sender Requirements
        AzureWorkItemTypeStateService workItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService, apiVersionAppender);
        AzureWorkItemCommentService workItemCommentService = new AzureWorkItemCommentService(azureHttpService, apiVersionAppender);

        IssueTrackerMessageSender<Integer> messageSender = azureBoardsMessageSenderFactory.createMessageSender(workItemService, workItemTypeStateService, workItemCommentService, organizationName, distributionDetails);

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private void installCustomFieldsIfNecessary(String organizationName, String projectName, String issueType, AzureProjectService projectService, AzureProcessService processService) throws AlertException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AzureCustomFieldManager azureCustomFieldInstaller = new AzureCustomFieldManager(organizationName, projectService, processService, executorService);
        try {
            azureCustomFieldInstaller.installCustomFields(projectName, issueType);
        } finally {
            executorService.shutdown();
        }
    }

}
