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
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

/**
 * Documentation:
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/work%20item%20type%20states?view=azure-devops-rest-5.1">Project WorkItemType States</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/processes/states?view=azure-devops-rest-5.1">Process WorkItemType States</a>
 */
public class AzureWorkItemTypeStateService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMTYPE_STATES = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitemtypes/{type}/states");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROCESS_WORKITEMTYPE_STATES = new AzureSpecTemplate("/{organization}/_apis/work/processes/{processId}/workItemTypes/{witRefName}/states");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROCESS_WORKITEMTYPE_STATE_INDIVIDUAL = new AzureSpecTemplate("/{organization}/_apis/work/processes/{processId}/workItemTypes/{witRefName}/states/{stateId}");

    public static final String PATH_ORGANIZATION_REPLACEMENT = "{organization}";
    public static final String PATH_PROJECT_REPLACEMENT = "{project}";
    public static final String PATH_TYPE_REPLACEMENT = "{type}";
    public static final String PATH_PROCESS_ID_REPLACEMENT = "{processId}";
    public static final String PATH_WIT_REF_NAME_REPLACEMENT = "{witRefName}";
    public static final String PATH_STATE_ID_REPLACEMENT = "{stateId}";

    private final AzureHttpService azureHttpService;

    public AzureWorkItemTypeStateService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<WorkItemTypeStateResponseModel> getStatesForProject(String organizationName, String projectIdOrName, String workItemType) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMTYPE_STATES
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_TYPE_REPLACEMENT, workItemType)
                                 .populateSpec();
        requestSpec = appendApiVersionQueryParam(requestSpec);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeStateResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public AzureArrayResponseModel<WorkItemTypeProcessStateResponseModel> getStatesForProcess(String organizationName, String processId, String workItemTypeRefName) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROCESS_WORKITEMTYPE_STATES
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROCESS_ID_REPLACEMENT, processId)
                                 .defineReplacement(PATH_WIT_REF_NAME_REPLACEMENT, workItemTypeRefName)
                                 .populateSpec();
        requestSpec = appendApiVersionQueryParam(requestSpec);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeProcessStateResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public WorkItemTypeProcessStateResponseModel getStatesForProcess(String organizationName, String processId, String workItemTypeRefName, String stateId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROCESS_WORKITEMTYPE_STATE_INDIVIDUAL
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROCESS_ID_REPLACEMENT, processId)
                                 .defineReplacement(PATH_WIT_REF_NAME_REPLACEMENT, workItemTypeRefName)
                                 .defineReplacement(PATH_STATE_ID_REPLACEMENT, stateId)
                                 .populateSpec();
        requestSpec = appendApiVersionQueryParam(requestSpec);
        return azureHttpService.get(requestSpec, WorkItemTypeProcessStateResponseModel.class);
    }

    private String appendApiVersionQueryParam(String requestSpec) {
        return String.format("%s?%s=%s", requestSpec, AzureHttpService.AZURE_API_VERSION_QUERY_PARAM_NAME, "5.1-preview.1");
    }

}
