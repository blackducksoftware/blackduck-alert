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
package com.synopsys.integration.azure.boards.common.service.workitem;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemDeletedResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

/**
 * Documentation:
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/work%20items?view=azure-devops-rest-5.1">Work Items</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/comments?view=azure-devops-rest-5.1">Work Item Comments</a>
 */
public class AzureWorkItemService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_WORKITEMS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/_apis/wit/workitems/{workItemId}");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitems?ids={ids}");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitems/{workItemId}");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_TYPE = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitems/${type}");

    public static final String PATH_ORGANIZATION_REPLACEMENT = "{organization}";
    public static final String PATH_PROJECT_REPLACEMENT = "{project}";
    public static final String PATH_WORK_ITEM_ID_REPLACEMENT = "{workItemId}";
    public static final String PATH_TYPE_REPLACEMENT = "{type}";
    public static final String PATH_IDS_REPLACEMENT = "{ids}";

    private final AzureHttpService azureHttpService;

    public AzureWorkItemService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<WorkItemResponseModel> getWorkItems(String organizationName, String projectIdOrName, Collection<Integer> workItemIds) throws HttpServiceException {
        String joinedWorkItemIds = workItemIds
                                       .stream()
                                       .map(Number::toString)
                                       .collect(Collectors.joining(","));
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_IDS_REPLACEMENT, joinedWorkItemIds)
                                 .populateSpec();
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public WorkItemResponseModel getWorkItem(String organizationName, Integer workItemId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_WORKITEMS_INDIVIDUAL
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_WORK_ITEM_ID_REPLACEMENT, workItemId.toString())
                                 .populateSpec();
        return azureHttpService.get(requestSpec, WorkItemResponseModel.class);
    }

    public WorkItemResponseModel getWorkItem(String organizationName, String projectIdOrName, Integer workItemId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_WORK_ITEM_ID_REPLACEMENT, workItemId.toString())
                                 .populateSpec();
        return azureHttpService.get(requestSpec, WorkItemResponseModel.class);
    }

    public WorkItemResponseModel createWorkItem(String organizationName, String projectIdOrName, String workItemType, WorkItemRequest workItemRequest) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_TYPE
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_TYPE_REPLACEMENT, workItemType)
                                 .populateSpec();
        try {
            HttpRequest httpRequest = buildWriteRequest(HttpMethods.POST, requestSpec, workItemRequest.getElementOperationModels());
            return azureHttpService.executeRequestAndParseResponse(httpRequest, WorkItemResponseModel.class);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

    public WorkItemResponseModel updateWorkItem(String organizationName, String projectIdOrName, Integer workItemId, WorkItemRequest workItemRequest) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_WORK_ITEM_ID_REPLACEMENT, workItemId.toString())
                                 .populateSpec();
        try {
            HttpRequest httpRequest = buildWriteRequest(HttpMethods.PATCH, requestSpec, workItemRequest.getElementOperationModels());
            return azureHttpService.executeRequestAndParseResponse(httpRequest, WorkItemResponseModel.class);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

    public WorkItemDeletedResponseModel deleteWorkItem(String organizationName, Integer workItemId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_WORK_ITEM_ID_REPLACEMENT, workItemId.toString())
                                 .populateSpec();
        return azureHttpService.delete(requestSpec, WorkItemDeletedResponseModel.class);
    }

    private HttpRequest buildWriteRequest(String httpMethod, String requestSpec, List<WorkItemElementOperationModel> requestModel) throws IOException {
        GenericUrl requestUrl = azureHttpService.constructRequestUrl(requestSpec);
        HttpRequest httpRequest = azureHttpService.buildRequestWithDefaultHeaders(httpMethod, requestUrl, requestModel);
        httpRequest.getHeaders().setContentType("application/json-patch+json");
        return httpRequest;
    }

}
