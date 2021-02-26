/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.azureboards2;

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
