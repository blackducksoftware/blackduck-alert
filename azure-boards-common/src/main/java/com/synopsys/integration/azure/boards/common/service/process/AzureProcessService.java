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
package com.synopsys.integration.azure.boards.common.service.process;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/processes/work%20item%20types/list?view=azure-devops-rest-5.1">Work Item Types</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/processes/fields/add?view=azure-devops-rest-5.1">Fields</a>
 */
public class AzureProcessService {
    private final AzureHttpService azureHttpService;

    public AzureProcessService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<ProcessWorkItemTypesResponseModel> getWorkItemTypes(String organizationName, String processId) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes?%s=%s", organizationName, processId);
        requestSpec = azureHttpService.appendApiVersion(requestSpec, AzureHttpService.AZURE_API_VERSION_5_1_PREVIEW_2);
        Type responseType = new TypeToken<AzureArrayResponseModel<ProcessWorkItemTypesResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public ProcessFieldResponseModel addFieldToWorkItemType(String organizationName, String processId, String workItemTypeRefName, ProcessFieldRequestModel requestBody) throws IOException, HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes/%s/fields?%s=%s", organizationName, processId, workItemTypeRefName);
        requestSpec = azureHttpService.appendApiVersion(requestSpec, AzureHttpService.AZURE_API_VERSION_5_1_PREVIEW_2);
        return azureHttpService.post(requestSpec, requestBody, ProcessFieldResponseModel.class);
    }

}
