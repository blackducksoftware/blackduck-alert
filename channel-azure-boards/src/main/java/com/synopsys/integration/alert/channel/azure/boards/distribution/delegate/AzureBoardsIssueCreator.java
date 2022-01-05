/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsHttpExceptionMessageImprover;
import com.synopsys.integration.alert.channel.azure.boards.distribution.search.AzureBoardsAlertIssuePropertiesManager;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsUILinkUtils;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class AzureBoardsIssueCreator extends IssueTrackerIssueCreator<Integer> {
    private final Gson gson;
    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemService workItemService;
    private final AzureBoardsAlertIssuePropertiesManager issuePropertiesManager;
    private final AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover;
    private final IssueCategoryRetriever issueCategoryRetriever;

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
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        super(channelKey, commenter, callbackInfoCreator);
        this.gson = gson;
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.workItemService = workItemService;
        this.issuePropertiesManager = issuePropertiesManager;
        this.exceptionMessageImprover = exceptionMessageImprover;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    @Override
    protected ExistingIssueDetails<Integer> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) throws AlertException {
        WorkItemRequest workItemCreationRequest = createWorkItemCreationRequest(alertIssueCreationModel);
        try {
            WorkItemResponseModel workItem = workItemService.createWorkItem(organizationName, distributionDetails.getProjectNameOrId(), distributionDetails.getWorkItemType(), workItemCreationRequest);
            return extractIssueDetails(workItem, alertIssueCreationModel);
        } catch (HttpServiceException e) {
            Optional<String> improvedExceptionMessage = exceptionMessageImprover.extractImprovedMessage(e);
            if (improvedExceptionMessage.isPresent()) {
                throw new AlertException(improvedExceptionMessage.get(), e);
            }
            throw new AlertException("Failed to create a work item in Azure Boards", e);
        }
    }

    @Override
    protected void assignAlertSearchProperties(ExistingIssueDetails<Integer> createdIssueDetails, ProjectIssueModel alertIssueSource) {
        // Although we can make this request here, it is more efficient to do this while creating the issue.
        // See the note in the method that creates the WorkItemRequest for more details.
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
