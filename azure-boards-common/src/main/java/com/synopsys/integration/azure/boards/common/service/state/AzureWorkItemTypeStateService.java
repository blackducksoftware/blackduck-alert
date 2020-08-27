/**
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.service.state;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;

/**
 * Documentation:
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/work%20item%20type%20states?view=azure-devops-rest-5.1">Project WorkItemType States</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/processes/states?view=azure-devops-rest-5.1">Process WorkItemType States</a>
 */
public class AzureWorkItemTypeStateService {
    private final AzureHttpService azureHttpService;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureWorkItemTypeStateService(AzureHttpService azureHttpService, AzureApiVersionAppender azureApiVersionAppender) {
        this.azureHttpService = azureHttpService;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public AzureArrayResponseModel<WorkItemTypeStateResponseModel> getStatesForProject(String organizationName, String projectIdOrName, String workItemType) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/_apis/wit/workitemtypes/%s/states", organizationName, projectIdOrName, workItemType);
        requestSpec = azureApiVersionAppender.appendApiVersion(requestSpec, AzureHttpService.AZURE_API_VERSION_5_1_PREVIEW_1);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeStateResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public AzureArrayResponseModel<WorkItemTypeProcessStateResponseModel> getStatesForProcess(String organizationName, String processId, String workItemTypeRefName) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes/%s/states", organizationName, processId, workItemTypeRefName);
        requestSpec = azureApiVersionAppender.appendApiVersion(requestSpec, AzureHttpService.AZURE_API_VERSION_5_1_PREVIEW_1);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeProcessStateResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public WorkItemTypeProcessStateResponseModel getStatesForProcess(String organizationName, String processId, String workItemTypeRefName, String stateId) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes/%s/states/%s", organizationName, processId, workItemTypeRefName, stateId);
        requestSpec = azureApiVersionAppender.appendApiVersion(requestSpec, AzureHttpService.AZURE_API_VERSION_5_1_PREVIEW_1);
        return azureHttpService.get(requestSpec, WorkItemTypeProcessStateResponseModel.class);
    }

}
