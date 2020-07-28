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
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentLengthValidator;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueHandler;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.synopsys.integration.azure.boards.common.service.query.WorkItemQueryResultResponseModel;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhere;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhereOperator;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemReferenceModel;
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
    private final AzureWorkItemQueryService azureWorkItemQueryService;

    public AzureBoardsIssueHandler(AzureBoardsProperties azureBoardsProperties, AzureBoardsMessageParser azureBoardsMessageParser, AzureWorkItemService azureWorkItemService,
        AzureWorkItemQueryService azureWorkItemQueryService) {
        super(CONTENT_LENGTH_VALIDATOR);
        this.azureBoardsProperties = azureBoardsProperties;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureWorkItemService = azureWorkItemService;
        this.azureWorkItemQueryService = azureWorkItemQueryService;
    }

    @Override
    protected Optional<WorkItemResponseModel> createIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        String azureOrganizationName = azureBoardsProperties.getOrganizationName();
        String azureProjectName = issueConfig.getProjectName();

        AzureBoardsSearchProperties issueProperties = request.getIssueSearchProperties();
        IssueContentModel issueContentModel = request.getRequestContent();
        if (!issueContentModel.getDescriptionComments().isEmpty() && !issueConfig.getCommentOnIssues()) {
            String description = truncateDescription(issueContentModel.getDescription());
            issueContentModel = IssueContentModel.of(issueContentModel.getTitle(), description, List.of());
        }

        WorkItemRequest workItemRequest = createWorkItemRequest(issueConfig.getIssueCreator(), issueContentModel, issueProperties);
        try {
            WorkItemResponseModel workItemResponseModel = azureWorkItemService.createWorkItem(azureOrganizationName, azureProjectName, issueConfig.getIssueType(), workItemRequest);
            Integer workItemId = workItemResponseModel.getId();
            logger.debug("Created new Azure Boards work item: {}", workItemId);
            if (issueConfig.getCommentOnIssues()) {
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
    protected List<WorkItemResponseModel> retrieveExistingIssues(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        AzureBoardsSearchProperties searchProperties = request.getIssueSearchProperties();
        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        WorkItemQueryWhere queryBuilder = WorkItemQuery
                                              .select(systemIdFieldName)
                                              .fromWorkItems()
                                              .whereGroup(AzureCustomFieldInstaller.ALERT_TOP_LEVEL_KEY_FIELD_NAME, WorkItemQueryWhereOperator.EQ, searchProperties.getTopLevelKey());
        Optional<String> optionalComponentLevelKey = searchProperties.getComponentLevelKey();
        if (optionalComponentLevelKey.isPresent()) {
            queryBuilder = queryBuilder.and(AzureCustomFieldInstaller.ALERT_COMPONENT_LEVEL_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, optionalComponentLevelKey.get());
        }

        WorkItemQuery query = queryBuilder.orderBy(systemIdFieldName).build();
        WorkItemQueryResultResponseModel workItemQueryResultResponseModel = azureWorkItemQueryService.queryForWorkItems(azureBoardsProperties.getOrganizationName(), issueConfig.getProjectName(), query);

        Set<Integer> workItemIds = workItemQueryResultResponseModel.getWorkItems()
                                       .stream()
                                       .map(WorkItemReferenceModel::getId)
                                       .collect(Collectors.toSet());
        AzureArrayResponseModel<WorkItemResponseModel> workItemArrayResponse = azureWorkItemService.getWorkItems(azureBoardsProperties.getOrganizationName(), issueConfig.getProjectName(), workItemIds);
        return workItemArrayResponse.getValue();
    }

    @Override
    protected boolean transitionIssue(WorkItemResponseModel issueModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException {
        // FIXME implement
        return false;
    }

    @Override
    protected void addComment(IssueConfig issueConfig, String workItemIdString, String comment) throws IntegrationException {
        Integer workItemId = Integer.valueOf(workItemIdString);
        addComment(azureBoardsProperties.getOrganizationName(), issueConfig.getProjectName(), workItemId, comment);
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
        AzureBoardsSearchProperties issueProperties = request.getIssueSearchProperties();
        String topLevelKey = issueProperties.getTopLevelKey();
        String componentLevelKey = issueProperties.getComponentLevelKey().orElse("Absent");
        logger.debug("Attempting the {} action in Azure Boards. Top Level Key: {}. Component Level Key: {}", request.getOperation().name(), topLevelKey, componentLevelKey);
    }

    private WorkItemRequest createWorkItemRequest(@Nullable String issueCreatorUniqueName, IssueContentModel issueContentModel, AzureBoardsSearchProperties issueSearchProperties) {
        List<WorkItemElementOperationModel> requestElementOps = new ArrayList<>();

        WorkItemElementOperationModel titleField = createAddFieldModel(WorkItemResponseFields.System_Title, issueContentModel.getTitle());
        requestElementOps.add(titleField);

        WorkItemElementOperationModel descriptionField = createAddFieldModel(WorkItemResponseFields.System_Description, issueContentModel.getDescription());
        requestElementOps.add(descriptionField);

        AzureFieldDefinition<String> alertTopLevelKeyFieldDefinition = AzureFieldDefinition.stringField(AzureCustomFieldInstaller.ALERT_TOP_LEVEL_KEY_FIELD_REFERENCE_NAME);
        WorkItemElementOperationModel alertTopLevelKey = createAddFieldModel(alertTopLevelKeyFieldDefinition, issueSearchProperties.getTopLevelKey());
        requestElementOps.add(alertTopLevelKey);

        Optional<String> optionalComponentLevelKey = issueSearchProperties.getComponentLevelKey();
        if (optionalComponentLevelKey.isPresent()) {
            AzureFieldDefinition<String> alertComponentLevelKeyFieldDefinition = AzureFieldDefinition.stringField(AzureCustomFieldInstaller.ALERT_COMPONENT_LEVEL_KEY_FIELD_REFERENCE_NAME);
            WorkItemElementOperationModel alertComponentLevelKeyField = createAddFieldModel(alertComponentLevelKeyFieldDefinition, optionalComponentLevelKey.get());
            requestElementOps.add(alertComponentLevelKeyField);
        }

        // TODO determine if we can support this
        // if (StringUtils.isNotBlank(issueCreatorUniqueName)) {
        // WorkItemUserModel workItemUserModel = new WorkItemUserModel(null, null, issueConfig.getIssueCreator(), null, null, null, null, null);
        // WorkItemElementOperationModel createdByField = createAddFieldModel(WorkItemResponseFields.System_CreatedBy, workItemUserModel);
        // requestElementOps.add(createdByField);
        // }
        return new WorkItemRequest(requestElementOps);
    }

    private <T> WorkItemElementOperationModel createAddFieldModel(AzureFieldDefinition<T> field, T value) {
        return WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.ADD, field, value);
    }

    private void addComment(String azureOrganizationName, String azureProjectName, Integer workItemId, String comment) throws IntegrationException {
        azureWorkItemService.addComment(azureOrganizationName, azureProjectName, workItemId, comment);
    }

}
