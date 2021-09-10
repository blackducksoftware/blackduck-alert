/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.synopsys.integration.azure.boards.common.service.process.AzureProcessService;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectResponseModel;
import com.synopsys.integration.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;

@Component
public class AzureBoardsProcessorFactory implements IssueTrackerProcessorFactory<AzureBoardsJobDetailsModel, Integer> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        AzureHttpService azureHttpService = azureBoardsProperties.createAzureHttpService(proxyManager.createProxyInfo(), gson);

        // Common Azure Boards Services
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
        AzureProjectService projectService = new AzureProjectService(azureHttpService, apiVersionAppender);
        AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService);
        AzureWorkItemQueryService workItemQueryService = new AzureWorkItemQueryService(azureHttpService, apiVersionAppender);

        String projectNameOrId = distributionDetails.getProjectNameOrId();
        String teamProjectName = retrieveProjectNameIfNecessary(projectService, organizationName, projectNameOrId);

        installCustomFieldsIfNecessary(
            organizationName,
            teamProjectName,
            distributionDetails.getWorkItemType(),
            projectService,
            new AzureProcessService(azureHttpService, apiVersionAppender)
        );

        // Extractor Requirements
        AzureBoardsIssueTrackerQueryManager queryManager = new AzureBoardsIssueTrackerQueryManager(organizationName, distributionDetails, workItemService, workItemQueryService);
        AzureBoardsSearcher azureBoardsSearcher = new AzureBoardsSearcher(gson, organizationName, queryManager, modelTransformer, teamProjectName);

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

    private String retrieveProjectNameIfNecessary(AzureProjectService projectService, String organizationName, String teamProjectNameOrId) {
        if (isStringPossibleUUID(teamProjectNameOrId)) {
            try {
                TeamProjectResponseModel teamProject = projectService.getProject(organizationName, teamProjectNameOrId);
                return teamProject.getName();
            } catch (HttpServiceException e) {
                logger.warn("Failed to look-up Azure Boards project name", e);
            }
        }
        return teamProjectNameOrId;
    }

    private boolean isStringPossibleUUID(String uuidCandidateString) {
        try {
            UUID.fromString(uuidCandidateString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
