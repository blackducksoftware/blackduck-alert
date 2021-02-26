/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.service;

import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TransitionHandler;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.exception.IntegrationException;

public class AzureTransitionHandler implements TransitionHandler<WorkItemTypeStateResponseModel> {
    public static final String WORK_ITEM_STATE_CATEGORY_PROPOSED = "Proposed";
    public static final String WORK_ITEM_STATE_CATEGORY_IN_PROGRESS = "InProgress";
    public static final String WORK_ITEM_STATE_CATEGORY_RESOLVED = "Resolved";
    public static final String WORK_ITEM_STATE_CATEGORY_COMPLETED = "Completed";

    private final Gson gson;
    private final AzureBoardsProperties azureBoardsProperties;
    private final AzureWorkItemService azureWorkItemService;
    private final AzureWorkItemTypeStateService azureWorkItemTypeStateService;

    public AzureTransitionHandler(Gson gson, AzureBoardsProperties azureBoardsProperties, AzureWorkItemService azureWorkItemService, AzureWorkItemTypeStateService azureWorkItemTypeStateService) {
        this.gson = gson;
        this.azureBoardsProperties = azureBoardsProperties;
        this.azureWorkItemService = azureWorkItemService;
        this.azureWorkItemTypeStateService = azureWorkItemTypeStateService;
    }

    @Override
    public String extractTransitionName(WorkItemTypeStateResponseModel workItemTypeState) {
        return workItemTypeState.getName();
    }

    @Override
    public List<WorkItemTypeStateResponseModel> retrieveIssueTransitions(String workItemIdString) throws IntegrationException {
        String organizationName = azureBoardsProperties.getOrganizationName();
        Integer workItemId = Integer.parseInt(workItemIdString);
        WorkItemResponseModel workItem = azureWorkItemService.getWorkItem(organizationName, workItemId);

        WorkItemFieldsWrapper fieldsWrapper = workItem.createFieldsWrapper(gson);
        Optional<String> optionalWorkItemProject = fieldsWrapper.getTeamProject();
        Optional<String> optionalWorkItemType = fieldsWrapper.getWorkItemType();
        if (optionalWorkItemProject.isPresent() && optionalWorkItemType.isPresent()) {
            AzureArrayResponseModel<WorkItemTypeStateResponseModel> workItemTypeStates = azureWorkItemTypeStateService.getStatesForProject(organizationName, optionalWorkItemProject.get(), optionalWorkItemType.get());
            return workItemTypeStates.getValue();
        }
        return List.of();
    }

    @Override
    public boolean doesTransitionToExpectedStatusCategory(WorkItemTypeStateResponseModel transition, String expectedStatusCategoryKey) {
        return transition.getCategory().equals(expectedStatusCategoryKey);
    }

}
