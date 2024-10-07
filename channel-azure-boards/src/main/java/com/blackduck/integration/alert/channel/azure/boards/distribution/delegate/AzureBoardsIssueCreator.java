/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCommenter;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCreator;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsHttpExceptionMessageImprover;
import com.blackduck.integration.alert.channel.azure.boards.distribution.search.AzureBoardsAlertIssuePropertiesManager;
import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsUILinkUtils;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.AzureWorkItemService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.WorkItemReferenceModel;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.blackduck.integration.alert.azure.boards.common.util.AzureFieldDefinition;

public class AzureBoardsIssueCreator extends IssueTrackerIssueCreator<Integer> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemService workItemService;
    private final AzureBoardsAlertIssuePropertiesManager issuePropertiesManager;
    private final AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final AzureWorkItemQueryService workItemQueryService;

    public AzureBoardsIssueCreator(
        AzureBoardsChannelKey channelKey,
        IssueTrackerIssueCommenter<Integer> commenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        Gson gson,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemService workItemService,
        AzureBoardsAlertIssuePropertiesManager issuePropertiesManager,
        AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover,
        IssueCategoryRetriever issueCategoryRetriever,
        AzureWorkItemQueryService workItemQueryService
    ) {
        super(channelKey, commenter, callbackInfoCreator);
        this.gson = gson;
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.workItemService = workItemService;
        this.issuePropertiesManager = issuePropertiesManager;
        this.exceptionMessageImprover = exceptionMessageImprover;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.workItemQueryService = workItemQueryService;
    }

    @Override
    protected ExistingIssueDetails<Integer> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) throws AlertException {
        Optional<ExistingIssueDetails<Integer>> existingIssue = doesIssueExist(alertIssueCreationModel);

        if (existingIssue.isPresent()) {
            return existingIssue.get();
        }
        ExistingIssueDetails<Integer> existingIssueDetails;
        WorkItemRequest workItemCreationRequest = createWorkItemCreationRequest(alertIssueCreationModel);
        try {
            WorkItemResponseModel workItem = workItemService.createWorkItem(
                organizationName,
                distributionDetails.getProjectNameOrId(),
                distributionDetails.getWorkItemType(),
                workItemCreationRequest
            );
            existingIssueDetails = extractIssueDetails(workItem, alertIssueCreationModel);
        } catch (HttpServiceException e) {
            Optional<String> improvedExceptionMessage = exceptionMessageImprover.extractImprovedMessage(e);
            if (improvedExceptionMessage.isPresent()) {
                throw new AlertException(improvedExceptionMessage.get(), e);
            }
            throw new AlertException("Failed to create a work item in Azure Boards", e);
        }

        return existingIssueDetails;
    }

    @Override
    protected void assignAlertSearchProperties(ExistingIssueDetails<Integer> createdIssueDetails, ProjectIssueModel alertIssueSource) {
        // Although we can make this request here, it is more efficient to do this while creating the issue.
        // See the note in the method that creates the WorkItemRequest for more details.
    }

    protected Optional<ExistingIssueDetails<Integer>> doesIssueExist(IssueCreationModel alertIssueCreationModel) {
        String query = alertIssueCreationModel.getQueryString().orElse(null);
        if (StringUtils.isBlank(query)) {
            return Optional.empty();
        }
        Optional<ExistingIssueDetails<Integer>> existingIssueDetails = Optional.empty();
        String projectNameOrId = distributionDetails.getProjectNameOrId();
        try {
            Optional<WorkItemReferenceModel> workItemReference = workItemQueryService.queryForWorkItems(organizationName, projectNameOrId, query)
                .getWorkItems().stream()
                .findFirst();
            if (workItemReference.isPresent()) {
                WorkItemResponseModel workItemResponseModel = workItemService.getWorkItem(organizationName, projectNameOrId, workItemReference.get().getId());
                existingIssueDetails = Optional.ofNullable(extractIssueDetails(workItemResponseModel, alertIssueCreationModel));
            }
        } catch (HttpServiceException ex) {
            logger.error("Query executed: {}", query);
            logger.error("Couldn't execute query to see if issue exists.", ex);
        }

        return existingIssueDetails;
    }

    private WorkItemRequest createWorkItemCreationRequest(IssueCreationModel alertIssueCreationModel) {
        List<WorkItemElementOperationModel> requestElementOps = new LinkedList<>();

        WorkItemElementOperationModel addTitleOp = createWorkItemAddOperation(WorkItemResponseFields.System_Title, alertIssueCreationModel.getTitle());
        requestElementOps.add(addTitleOp);

        WorkItemElementOperationModel addDescriptionOp = createWorkItemAddOperation(WorkItemResponseFields.System_Description, alertIssueCreationModel.getDescription());
        requestElementOps.add(addDescriptionOp);

        // Note: If a ProjectIssueModel is present, Alert Search Properties are assigned during issue-creation
        Optional<ProjectIssueModel> issueSource = alertIssueCreationModel.getSource();
        if (issueSource.isPresent()) {
            List<WorkItemElementOperationModel> alertSearchFieldOps = issuePropertiesManager.createWorkItemRequestCustomFieldOperations(issueSource.get());
            requestElementOps.addAll(alertSearchFieldOps);
        }

        return new WorkItemRequest(requestElementOps);
    }

    private <T> WorkItemElementOperationModel createWorkItemAddOperation(AzureFieldDefinition<T> field, T value) {
        return WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.ADD, field, value);
    }

    private ExistingIssueDetails<Integer> extractIssueDetails(WorkItemResponseModel workItem, IssueCreationModel alertIssueCreationModel) {
        WorkItemFieldsWrapper workItemFields = workItem.createFieldsWrapper(gson);
        String workItemKey = Objects.toString(workItem.getId());
        String workItemTitle = workItemFields.getField(WorkItemResponseFields.System_Title).orElse("Unknown Title");
        String workItemUILink = AzureBoardsUILinkUtils.extractUILink(organizationName, workItem);

        IssueCategory issueCategory = alertIssueCreationModel.getSource()
            .map(issueCategoryRetriever::retrieveIssueCategoryFromProjectIssueModel)
            .orElse(IssueCategory.BOM);

        return new ExistingIssueDetails<>(workItem.getId(), workItemKey, workItemTitle, workItemUILink, IssueStatus.RESOLVABLE, issueCategory);
    }

}
