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

import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TransitionHandler;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldsExtractor;
import com.synopsys.integration.exception.IntegrationException;

public class AzureTransitionHandler implements TransitionHandler<WorkItemTypeStateResponseModel> {
    public static final String WORK_ITEM_STATE_CATEGORY_PROPOSED = "Proposed";
    public static final String WORK_ITEM_STATE_CATEGORY_IN_PROGRESS = "InProgress";
    public static final String WORK_ITEM_STATE_CATEGORY_RESOLVED = "Resolved";
    public static final String WORK_ITEM_STATE_CATEGORY_COMPLETED = "Completed";

    private final AzureBoardsProperties azureBoardsProperties;
    private final AzureWorkItemService azureWorkItemService;
    private final AzureWorkItemTypeStateService azureWorkItemTypeStateService;
    private final AzureFieldsExtractor azureFieldsExtractor;

    public AzureTransitionHandler(Gson gson, AzureBoardsProperties azureBoardsProperties, AzureWorkItemService azureWorkItemService, AzureWorkItemTypeStateService azureWorkItemTypeStateService) {
        this.azureBoardsProperties = azureBoardsProperties;
        this.azureWorkItemService = azureWorkItemService;
        this.azureWorkItemTypeStateService = azureWorkItemTypeStateService;
        this.azureFieldsExtractor = new AzureFieldsExtractor(gson);
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

        JsonObject workItemFields = workItem.getFields();
        Optional<String> optionalWorkItemProject = azureFieldsExtractor.extractField(workItemFields, WorkItemResponseFields.System_TeamProject);
        Optional<String> optionalWorkItemType = azureFieldsExtractor.extractField(workItemFields, WorkItemResponseFields.System_WorkItemType);
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
