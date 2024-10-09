/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.model.AzureArrayResponseModel;
import com.blackduck.integration.alert.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.blackduck.integration.alert.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.AzureWorkItemService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.google.gson.Gson;

public class AzureBoardsWorkItemTypeStateRetriever {
    private final Gson gson;
    private final AzureWorkItemService azureWorkItemService;
    private final AzureWorkItemTypeStateService azureWorkItemTypeStateService;

    public AzureBoardsWorkItemTypeStateRetriever(Gson gson, AzureWorkItemService azureWorkItemService, AzureWorkItemTypeStateService azureWorkItemTypeStateService) {
        this.gson = gson;
        this.azureWorkItemService = azureWorkItemService;
        this.azureWorkItemTypeStateService = azureWorkItemTypeStateService;
    }

    public List<WorkItemTypeStateResponseModel> retrieveAvailableWorkItemStates(String organizationName, Integer workItemId) throws HttpServiceException {
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

}
