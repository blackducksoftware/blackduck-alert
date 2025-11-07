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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerModelExtractor;
import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerProcessorFactory;
import com.blackduck.integration.alert.api.channel.issue.tracker.convert.ProjectMessageToIssueModelTransformer;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.azure.boards.common.http.AzureApiVersionAppender;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreator;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpService;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.service.process.AzureProcessService;
import com.blackduck.integration.alert.azure.boards.common.service.project.AzureProjectService;
import com.blackduck.integration.alert.azure.boards.common.service.project.TeamProjectResponseModel;
import com.blackduck.integration.alert.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.AzureWorkItemService;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsComponentIssueFinder;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsExistingIssueDetailsCreator;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsIssueStatusResolver;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsProjectAndVersionIssueFinder;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsWorkItemFinder;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureCustomFieldManager;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

@Component
public class AzureBoardsProcessorFactory implements IssueTrackerProcessorFactory<AzureBoardsJobDetailsModel, Integer> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final AzureBoardsMessageFormatter formatter;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory;
    private final ProxyManager proxyManager;
    private final ProjectMessageToIssueModelTransformer modelTransformer;
    private final IssueCategoryRetriever issueCategoryRetriever;

    @Autowired
    public AzureBoardsProcessorFactory(
        Gson gson,
        AzureBoardsMessageFormatter formatter,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureBoardsMessageSenderFactory azureBoardsMessageSenderFactory,
        ProxyManager proxyManager,
        ProjectMessageToIssueModelTransformer modelTransformer,
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        this.gson = gson;
        this.formatter = formatter;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureBoardsMessageSenderFactory = azureBoardsMessageSenderFactory;
        this.proxyManager = proxyManager;
        this.modelTransformer = modelTransformer;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    @Override
    public IssueTrackerProcessor<Integer> createProcessor(AzureBoardsJobDetailsModel distributionDetails, UUID jobExecutionId, Set<Long> notificationIds)
        throws AlertException {
        AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.createAzureBoardsPropertiesWithJobId(distributionDetails.getJobId());
        String organizationName = azureBoardsProperties.getOrganizationName();
        azureBoardsProperties.validateProperties();

        // Initialize Http Service
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
        AzureHttpRequestCreator azureHttpRequestCreator = azureBoardsProperties.createAzureHttpRequestCreator(proxy, gson);
        AzureHttpService azureHttpService = new AzureHttpService(gson, azureHttpRequestCreator);

        // Common Azure Boards Services
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
        AzureProjectService projectService = new AzureProjectService(azureHttpService, apiVersionAppender);
        AzureWorkItemService workItemService = new AzureWorkItemService(azureHttpService, azureHttpRequestCreator);
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

        // Searcher Requirements
        AzureBoardsIssueStatusResolver azureBoardsIssueStatusResolver = new AzureBoardsIssueStatusResolver(
            distributionDetails.getWorkItemCompletedState(),
            distributionDetails.getWorkItemReopenState()
        );
        AzureBoardsIssueTrackerQueryManager queryManager = new AzureBoardsIssueTrackerQueryManager(organizationName, distributionDetails, workItemService, workItemQueryService);

        // Extractor Requirements
        AzureBoardsExistingIssueDetailsCreator issueDetailsCreator = new AzureBoardsExistingIssueDetailsCreator(
            organizationName,
            issueCategoryRetriever,
            azureBoardsIssueStatusResolver
        );
        AzureBoardsWorkItemFinder workItemFinder = new AzureBoardsWorkItemFinder(queryManager, teamProjectName);
        AzureBoardsProjectAndVersionIssueFinder projectAndVersionIssueFinder = new AzureBoardsProjectAndVersionIssueFinder(gson, issueDetailsCreator, workItemFinder);
        AzureBoardsComponentIssueFinder componentIssueFinder = new AzureBoardsComponentIssueFinder(gson, workItemFinder, issueDetailsCreator);
        IssueTrackerSearcher<Integer> azureBoardsSearcher = new IssueTrackerSearcher<>(
            projectAndVersionIssueFinder,
            projectAndVersionIssueFinder,
            componentIssueFinder,
            componentIssueFinder,
            modelTransformer
        );

        IssueTrackerModelExtractor<Integer> extractor = new IssueTrackerModelExtractor<>(formatter, azureBoardsSearcher);

        IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<Integer>> messageSender = azureBoardsMessageSenderFactory.createAsyncMessageSender(
            distributionDetails,
            null,
            jobExecutionId,
            notificationIds
        );

        return new IssueTrackerProcessor<>(extractor, messageSender);
    }

    private void installCustomFieldsIfNecessary(
        String organizationName,
        String projectName,
        String issueType,
        AzureProjectService projectService,
        AzureProcessService processService
    ) throws AlertException {
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
