/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.workitem;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreator;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemDeletedResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

/**
 * Documentation:
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/work%20items?view=azure-devops-rest-5.1">Work Items</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/comments?view=azure-devops-rest-5.1">Work Item Comments</a>
 */
public class AzureWorkItemService {
    private final AzureHttpService azureHttpService;
    private final AzureHttpRequestCreator azureHttpRequestCreator;

    public AzureWorkItemService(AzureHttpService azureHttpService, AzureHttpRequestCreator azureHttpRequestCreator) {
        this.azureHttpService = azureHttpService;
        this.azureHttpRequestCreator = azureHttpRequestCreator;
    }

    public AzureArrayResponseModel<WorkItemResponseModel> getWorkItems(String organizationName, String projectIdOrName, Collection<Integer> workItemIds) throws HttpServiceException {
        String joinedWorkItemIds = StringUtils.join(workItemIds, ",");
        String requestSpec = String.format("/%s/%s/_apis/wit/workitems?ids=%s", organizationName, projectIdOrName, joinedWorkItemIds);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public WorkItemResponseModel getWorkItem(String organizationName, Integer workItemId) throws HttpServiceException {
        String requestSpec = createWorkItemSpecById(organizationName, workItemId);
        return azureHttpService.get(requestSpec, WorkItemResponseModel.class);
    }

    public WorkItemResponseModel getWorkItem(String organizationName, String projectIdOrName, Integer workItemId) throws HttpServiceException {
        String requestSpec = createWorkItemSpecByIdWithProject(organizationName, projectIdOrName, workItemId);
        return azureHttpService.get(requestSpec, WorkItemResponseModel.class);
    }

    public WorkItemResponseModel createWorkItem(String organizationName, String projectIdOrName, String workItemType, WorkItemRequest workItemRequest) throws HttpServiceException {
        String requestSpec = createWorkItemSpecWithProject(organizationName, projectIdOrName, workItemType);
        return azureHttpService.post(requestSpec, workItemRequest.getElementOperationModels(), WorkItemResponseModel.class, AzureHttpRequestCreator.CONTENT_TYPE_JSON_PATCH);
    }

    public WorkItemResponseModel updateWorkItem(String organizationName, String projectIdOrName, Integer workItemId, WorkItemRequest workItemRequest) throws HttpServiceException {
        String requestSpec = createWorkItemSpecByIdWithProject(organizationName, projectIdOrName, workItemId);
        try {
            HttpRequest httpRequest = buildWriteRequest(HttpMethods.PATCH, requestSpec, workItemRequest.getElementOperationModels());
            return azureHttpService.executeRequestAndParseResponse(httpRequest, WorkItemResponseModel.class);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

    public WorkItemDeletedResponseModel deleteWorkItem(String organizationName, Integer workItemId) throws HttpServiceException {
        String requestSpec = createWorkItemSpecById(organizationName, workItemId);
        return azureHttpService.delete(requestSpec, WorkItemDeletedResponseModel.class);
    }

    private HttpRequest buildWriteRequest(String httpMethod, String requestSpec, List<WorkItemElementOperationModel> requestModel) throws IOException {
        GenericUrl requestUrl = azureHttpRequestCreator.createRequestUrl(requestSpec);
        HttpRequest httpRequest = azureHttpRequestCreator.createRequestWithDefaultHeaders(httpMethod, requestUrl, requestModel);
        httpRequest.getHeaders().setContentType(AzureHttpRequestCreator.CONTENT_TYPE_JSON_PATCH);
        return httpRequest;
    }

    private String createWorkItemSpecWithProject(String organizationName, String projectIdOrName, String workItemType) {
        return String.format("/%s/%s/_apis/wit/workitems/$%s", organizationName, projectIdOrName, workItemType);
    }

    private String createWorkItemSpecByIdWithProject(String organizationName, String projectIdOrName, Integer workItemId) {
        return String.format("/%s/%s/_apis/wit/workitems/%s", organizationName, projectIdOrName, workItemId.toString());
    }

    private String createWorkItemSpecById(String organizationName, Integer workItemId) {
        return String.format("/%s/_apis/wit/workitems/%s", organizationName, workItemId.toString());
    }

}
