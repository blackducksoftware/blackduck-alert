/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

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
