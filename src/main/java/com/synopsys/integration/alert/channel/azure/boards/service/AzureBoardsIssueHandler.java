/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.azure.boards.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsIssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentLengthValidator;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueHandler;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class AzureBoardsIssueHandler extends IssueHandler<WorkItemResponseModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final IssueContentLengthValidator CONTENT_LENGTH_VALIDATOR = new IssueContentLengthValidator(
        AzureBoardsMessageParser.TITLE_SIZE_LIMIT,
        AzureBoardsMessageParser.MESSAGE_SIZE_LIMIT,
        AzureBoardsMessageParser.MESSAGE_SIZE_LIMIT
    );

    private final AzureBoardsProperties azureBoardsProperties;
    private final AzureBoardsMessageParser azureBoardsMessageParser;
    private final AzureWorkItemService azureWorkItemService;

    public AzureBoardsIssueHandler(AzureBoardsProperties azureBoardsProperties, AzureBoardsMessageParser azureBoardsMessageParser, AzureWorkItemService azureWorkItemService) {
        super(CONTENT_LENGTH_VALIDATOR);
        this.azureBoardsProperties = azureBoardsProperties;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureWorkItemService = azureWorkItemService;
    }

    @Override
    protected Optional<WorkItemResponseModel> createIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        AzureBoardsIssueConfig azureBoardsIssueConfig = (AzureBoardsIssueConfig) issueConfig;
        String azureOrganizationName = azureBoardsIssueConfig.getOrganizationName();
        String azureProjectName = azureBoardsIssueConfig.getProjectName();

        IssueSearchProperties issueProperties = request.getIssueSearchProperties();
        IssueContentModel issueContentModel = request.getRequestContent();
        if (!issueContentModel.getDescriptionComments().isEmpty() && !azureBoardsIssueConfig.getCommentOnIssues()) {
            String description = truncateDescription(issueContentModel.getDescription());
            issueContentModel = IssueContentModel.of(issueContentModel.getTitle(), description, List.of());
        }

        WorkItemRequest workItemRequest = createWorkItemRequest(issueConfig.getIssueCreator(), issueContentModel);
        try {
            WorkItemResponseModel workItemResponseModel = azureWorkItemService.createWorkItem(azureOrganizationName, azureProjectName, azureBoardsIssueConfig.getIssueType(), workItemRequest);
            Integer workItemId = workItemResponseModel.getId();
            logger.debug("Created new Azure Boards work item: {}", workItemId);
            addWorkItemProperties(workItemId, issueProperties);
            if (azureBoardsIssueConfig.getCommentOnIssues()) {
                addComment(azureOrganizationName, azureProjectName, workItemId, "This issue was automatically created by Alert.");
                for (String additionalComment : issueContentModel.getDescriptionComments()) {
                    String comment = String.format("%s %s %s", DESCRIPTION_CONTINUED_TEXT, azureBoardsMessageParser.getLineSeparator(), additionalComment);
                    addComment(azureOrganizationName, azureProjectName, workItemId, comment);
                }
            }
            return Optional.of(workItemResponseModel);
        } catch (IntegrationRestException e) {
            logger.error("Error creating issue", e);
        }
        return Optional.empty();
    }

    @Override
    protected List<WorkItemResponseModel> retrieveExistingIssues(String projectSearchIdentifier, IssueTrackerRequest request) throws IntegrationException {
        // FIXME implement
        return null;
    }

    @Override
    protected boolean transitionIssue(WorkItemResponseModel issueModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException {
        // FIXME implement
        return false;
    }

    @Override
    protected void addComment(IssueConfig issueConfig, String workItemIdString, String comment) throws IntegrationException {
        AzureBoardsIssueConfig azureBoardsIssueConfig = (AzureBoardsIssueConfig) issueConfig;
        Integer workItemId = Integer.valueOf(workItemIdString);
        addComment(azureBoardsIssueConfig.getOrganizationName(), azureBoardsIssueConfig.getProjectName(), workItemId, comment);
    }

    @Override
    protected String getIssueKey(WorkItemResponseModel issueModel) {
        return issueModel.getId().toString();
    }

    @Override
    protected IssueTrackerIssueResponseModel createResponseModel(AlertIssueOrigin alertIssueOrigin, String issueTitle, IssueOperation issueOperation, WorkItemResponseModel issueResponse) {
        Integer workItemId = issueResponse.getId();
        Map<String, ReferenceLinkModel> issueLinks = issueResponse.getLinks();
        ReferenceLinkModel htmlLink = issueLinks.get("html");
        String uiLink = Optional.ofNullable(htmlLink)
                            .map(ReferenceLinkModel::getHref)
                            .orElseGet(this::getIssueTrackerUrl);
        return new IssueTrackerIssueResponseModel(alertIssueOrigin, workItemId.toString(), uiLink, issueTitle, issueOperation);
    }

    @Override
    protected String getIssueTrackerUrl() {
        String url = String.format("%s/%s", AzureHttpServiceFactory.DEFAULT_BASE_URL, azureBoardsProperties.getOrganizationName());
        return new GenericUrl(url).build();
    }

    @Override
    protected void logIssueAction(String issueTrackerProjectName, IssueTrackerRequest request) {
        // FIXME implement or remove abstraction
    }

    private WorkItemRequest createWorkItemRequest(@Nullable String issueCreatorUniqueName, IssueContentModel issueContentModel) {
        List<WorkItemElementOperationModel> requestElementOps = new ArrayList<>();

        WorkItemElementOperationModel titleField = createAddFieldModel(WorkItemResponseFields.System_Title, issueContentModel.getTitle());
        requestElementOps.add(titleField);

        WorkItemElementOperationModel descriptionField = createAddFieldModel(WorkItemResponseFields.System_Description, issueContentModel.getDescription());
        requestElementOps.add(descriptionField);

        // FIXME determine if we can support this
        // if (StringUtils.isNotBlank(issueCreatorUniqueName)) {
        // WorkItemUserModel workItemUserModel = new WorkItemUserModel(null, null, issueConfig.getIssueCreator(), null, null, null, null, null);
        // WorkItemElementOperationModel createdByField = createAddFieldModel(WorkItemResponseFields.System_CreatedBy, workItemUserModel);
        // requestElementOps.add(createdByField);
        // }
        return new WorkItemRequest(requestElementOps);
    }

    private void addWorkItemProperties(Integer workItemId, IssueSearchProperties issueSearchProperties) {
        // FIXME implement
    }

    private <T> WorkItemElementOperationModel createAddFieldModel(AzureFieldDefinition<T> field, T value) {
        return WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.ADD, field, value);
    }

    private void addComment(String azureOrganizationName, String azureProjectName, Integer workItemId, String comment) throws IntegrationException {
        azureWorkItemService.addComment(azureOrganizationName, azureProjectName, workItemId, comment);
    }

}
