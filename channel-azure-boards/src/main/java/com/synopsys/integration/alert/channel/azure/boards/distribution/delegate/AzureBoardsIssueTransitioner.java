/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.delegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsHttpExceptionMessageImprover;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsWorkItemTypeStateRetriever;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsIssueTransitioner extends IssueTrackerIssueTransitioner<Integer> {
    public static final String WORK_ITEM_STATE_CATEGORY_PROPOSED = "Proposed";
    public static final String WORK_ITEM_STATE_CATEGORY_IN_PROGRESS = "InProgress";
    public static final String WORK_ITEM_STATE_CATEGORY_RESOLVED = "Resolved";
    public static final String WORK_ITEM_STATE_CATEGORY_COMPLETED = "Completed";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemService workItemService;
    private final AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever;
    private final AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover;

    public AzureBoardsIssueTransitioner(
        IssueTrackerIssueCommenter<Integer> commenter,
        IssueTrackerIssueResponseCreator issueResponseCreator,
        Gson gson,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemService workItemService,
        AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever,
        AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover
    ) {
        super(commenter, issueResponseCreator);
        this.gson = gson;
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.workItemService = workItemService;
        this.workItemTypeStateRetriever = workItemTypeStateRetriever;
        this.exceptionMessageImprover = exceptionMessageImprover;
    }

    @Override
    protected Optional<String> retrieveJobTransitionName(IssueOperation issueOperation) {
        String nullableTransitionName = null;
        if (IssueOperation.OPEN.equals(issueOperation)) {
            nullableTransitionName = distributionDetails.getWorkItemReopenState();
        } else if (IssueOperation.RESOLVE.equals(issueOperation)) {
            nullableTransitionName = distributionDetails.getWorkItemCompletedState();
        }
        return Optional.ofNullable(nullableTransitionName).filter(StringUtils::isNotBlank);
    }

    @Override
    protected boolean isTransitionRequired(ExistingIssueDetails<Integer> existingIssueDetails, IssueOperation issueOperation) throws AlertException {
        Integer issueId = existingIssueDetails.getIssueId();
        WorkItemResponseModel workItem = retrieveWorkItem(issueId);

        List<WorkItemTypeStateResponseModel> availableStates = retrieveAvailableStates(issueId);
        Map<String, String> stateNameToCategory = mapStateNameToCategory(availableStates);

        WorkItemFieldsWrapper fieldsWrapper = workItem.createFieldsWrapper(gson);
        Optional<String> optionalCurrentState = fieldsWrapper.getField(WorkItemResponseFields.System_State);
        if (optionalCurrentState.isPresent()) {
            String workItemStateCategory = stateNameToCategory.get(optionalCurrentState.get());
            boolean isOpen = WORK_ITEM_STATE_CATEGORY_PROPOSED.equals(workItemStateCategory);
            boolean isResolved = WORK_ITEM_STATE_CATEGORY_COMPLETED.equals(workItemStateCategory);

            if (IssueOperation.OPEN.equals(issueOperation)) {
                return !isOpen;
            } else if (IssueOperation.RESOLVE.equals(issueOperation)) {
                return !isResolved;
            } else {
                return true;
            }
        } else {
            logger.warn("Could not get the work item state. Work Item ID: {}", issueId);
        }
        return false;
    }

    @Override
    protected void findAndPerformTransition(ExistingIssueDetails<Integer> existingIssueDetails, String transitionName) throws AlertException {
        WorkItemElementOperationModel replaceSystemStateField = WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.REPLACE, WorkItemResponseFields.System_State, transitionName);
        WorkItemRequest request = new WorkItemRequest(List.of(replaceSystemStateField));

        Integer issueId = existingIssueDetails.getIssueId();
        try {
            workItemService.updateWorkItem(organizationName, distributionDetails.getProjectNameOrId(), issueId, request);
        } catch (HttpServiceException e) {
            List<String> availableStates = retrieveAvailableStates(existingIssueDetails.getIssueId())
                                               .stream()
                                               .map(WorkItemTypeStateResponseModel::getName)
                                               .collect(Collectors.toList());
            throw new IssueMissingTransitionException(existingIssueDetails.getIssueKey(), transitionName, availableStates);
        }
    }

    private WorkItemResponseModel retrieveWorkItem(Integer issueId) throws AlertException {
        try {
            return workItemService.getWorkItem(organizationName, issueId);
        } catch (HttpServiceException e) {
            throw improveExceptionOrDefault(e, String.format("Failed to retrieve available state categories from Azure. Work Item ID: %s", issueId));
        }
    }

    private Map<String, String> mapStateNameToCategory(List<WorkItemTypeStateResponseModel> workItemTypeStates) {
        return workItemTypeStates
                   .stream()
                   .collect(Collectors.toMap(WorkItemTypeStateResponseModel::getName, WorkItemTypeStateResponseModel::getCategory));
    }

    private List<WorkItemTypeStateResponseModel> retrieveAvailableStates(Integer issueId) throws AlertException {
        try {
            return workItemTypeStateRetriever.retrieveAvailableWorkItemStates(organizationName, issueId);
        } catch (HttpServiceException e) {
            throw improveExceptionOrDefault(e, String.format("Failed to retrieve available work item states from Azure. Work Item ID: %s", issueId));
        }
    }

    private AlertException improveExceptionOrDefault(HttpServiceException e, String defaultExceptionMessage) {
        return exceptionMessageImprover.extractImprovedMessage(e)
                   .map(improvedException -> new AlertException(improvedException, e))
                   .orElseGet(() -> new AlertException(defaultExceptionMessage, e));
    }

}
