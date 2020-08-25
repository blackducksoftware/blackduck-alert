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
import com.google.gson.JsonElement;
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
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
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

    private final AzureBoardsProperties azureBoardsProperties;
    private final AzureBoardsMessageParser azureBoardsMessageParser;
    private final AzureWorkItemService azureWorkItemService;
    private final AzureWorkItemCommentService azureWorkItemCommentService;
    private final AzureWorkItemQueryService azureWorkItemQueryService;

    public AzureBoardsIssueHandler(IssueContentLengthValidator issueContentLengthValidator, AzureBoardsProperties azureBoardsProperties,
        AzureBoardsMessageParser azureBoardsMessageParser, AzureWorkItemService azureWorkItemService, AzureWorkItemCommentService azureWorkItemCommentService,
        AzureWorkItemQueryService azureWorkItemQueryService) {
        super(issueContentLengthValidator);
        this.azureBoardsProperties = azureBoardsProperties;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureWorkItemService = azureWorkItemService;
        this.azureWorkItemCommentService = azureWorkItemCommentService;
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
                                              .whereGroup(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_NAME, WorkItemQueryWhereOperator.EQ, searchProperties.getTopicKey());
        Optional<String> optionalComponentLevelKey = searchProperties.getComponentKey();
        if (optionalComponentLevelKey.isPresent()) {
            queryBuilder = queryBuilder.and(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, optionalComponentLevelKey.get());
        }

        WorkItemQuery query = queryBuilder.orderBy(systemIdFieldName).build();
        WorkItemQueryResultResponseModel workItemQueryResultResponseModel = azureWorkItemQueryService.queryForWorkItems(azureBoardsProperties.getOrganizationName(), issueConfig.getProjectName(), query);

        Set<Integer> workItemIds = workItemQueryResultResponseModel.getWorkItems()
                                       .stream()
                                       .map(WorkItemReferenceModel::getId)
                                       .collect(Collectors.toSet());
        if (!workItemIds.isEmpty()) {
            AzureArrayResponseModel<WorkItemResponseModel> workItemArrayResponse = azureWorkItemService.getWorkItems(azureBoardsProperties.getOrganizationName(), issueConfig.getProjectName(), workItemIds);
            return workItemArrayResponse.getValue();
        }
        return List.of();
    }

    @Override
    protected boolean transitionIssue(WorkItemResponseModel issueModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException {
        List<WorkItemElementOperationModel> requestElementOps = new ArrayList<>();
        Optional<String> transition;
        if (IssueOperation.RESOLVE.equals(operation)) {
            transition = issueConfig.getResolveTransition();
        } else {
            transition = issueConfig.getOpenTransition();
        }
        if (transition.isPresent()) {
            String transitionName = transition.get();
            try {
                WorkItemElementOperationModel descriptionField = createUpdateFieldModel(WorkItemResponseFields.System_State, transitionName);
                requestElementOps.add(descriptionField);
                WorkItemRequest request = new WorkItemRequest(requestElementOps);
                WorkItemResponseModel workItemResponse = azureWorkItemService.updateWorkItem(azureBoardsProperties.getOrganizationName(), issueConfig.getProjectName(), issueModel.getId(), request);
                JsonElement stateElement = workItemResponse.getFields().get(WorkItemResponseFields.System_State.getFieldName());
                return transitionName.equals(stateElement.getAsString());
            } catch (HttpServiceException ex) {
                logger.error("Error transitioning work item {} to {}: cause: {}", issueModel.getId(), transitionName, ex);
            }
        }

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
        // AzureWorkItemResponse does not contain any links other than when a work item is created.
        String uiLink = Optional.ofNullable(issueLinks)
                            .flatMap(issueLinkMap -> Optional.ofNullable(issueLinkMap.get("html")))
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
        String topLevelKey = issueProperties.getTopicKey();
        String componentLevelKey = issueProperties.getComponentKey().orElse("Absent");
        logger.debug("Attempting the {} action in Azure Boards. Top Level Key: {}. Component Level Key: {}", request.getOperation().name(), topLevelKey, componentLevelKey);
    }

    private WorkItemRequest createWorkItemRequest(@Nullable String issueCreatorUniqueName, IssueContentModel issueContentModel, AzureBoardsSearchProperties issueSearchProperties) {
        List<WorkItemElementOperationModel> requestElementOps = new ArrayList<>();

        WorkItemElementOperationModel titleField = createAddFieldModel(WorkItemResponseFields.System_Title, issueContentModel.getTitle());
        requestElementOps.add(titleField);

        WorkItemElementOperationModel descriptionField = createAddFieldModel(WorkItemResponseFields.System_Description, issueContentModel.getDescription());
        requestElementOps.add(descriptionField);

        // TODO determine if we can support this
        // if (StringUtils.isNotBlank(issueCreatorUniqueName)) {
        // WorkItemUserModel workItemUserModel = new WorkItemUserModel(null, null, issueConfig.getIssueCreator(), null, null, null, null, null);
        // WorkItemElementOperationModel createdByField = createAddFieldModel(WorkItemResponseFields.System_CreatedBy, workItemUserModel);
        // requestElementOps.add(createdByField);
        // }

        List<WorkItemElementOperationModel> alertAzureCustomFields = createWorkItemRequestCustomFields(issueSearchProperties);
        requestElementOps.addAll(alertAzureCustomFields);

        return new WorkItemRequest(requestElementOps);
    }

    private List<WorkItemElementOperationModel> createWorkItemRequestCustomFields(AzureBoardsSearchProperties issueSearchProperties) {
        List<WorkItemElementOperationModel> customFields = new ArrayList<>(7);
        addStringField(customFields, AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getProviderKey());
        addStringField(customFields, AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getTopicKey());
        addStringField(customFields, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getSubTopicKey());
        addStringField(customFields, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getCategoryKey());
        addStringField(customFields, AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getComponentKey());
        addStringField(customFields, AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getSubComponentKey());
        addStringField(customFields, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, issueSearchProperties.getAdditionalInfoKey());
        return customFields;
    }

    private void addStringField(List<WorkItemElementOperationModel> customFields, String fieldReferenceName, Optional<String> optionalFieldValue) {
        optionalFieldValue.ifPresent(fieldValue -> addStringField(customFields, fieldReferenceName, fieldValue));
    }

    private void addStringField(List<WorkItemElementOperationModel> customFields, String fieldReferenceName, String fieldValue) {
        AzureFieldDefinition<String> alertProviderKeyFieldDefinition = AzureFieldDefinition.stringField(fieldReferenceName);
        WorkItemElementOperationModel alertProviderKeyField = createAddFieldModel(alertProviderKeyFieldDefinition, fieldValue);
        customFields.add(alertProviderKeyField);
    }

    private <T> WorkItemElementOperationModel createAddFieldModel(AzureFieldDefinition<T> field, T value) {
        return WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.ADD, field, value);
    }

    private <T> WorkItemElementOperationModel createUpdateFieldModel(AzureFieldDefinition<T> field, T value) {
        return WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.REPLACE, field, value);
    }

    private void addComment(String azureOrganizationName, String azureProjectName, Integer workItemId, String comment) throws IntegrationException {
        azureWorkItemCommentService.addComment(azureOrganizationName, azureProjectName, workItemId, comment);
    }

}
