/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.state;

import java.lang.reflect.Type;

import com.blackduck.integration.alert.azure.boards.common.http.AzureApiVersionAppender;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpService;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.model.AzureArrayResponseModel;
import com.google.gson.reflect.TypeToken;

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

    public AzureArrayResponseModel<WorkItemTypeStateResponseModel> getStatesForProject(String organizationName, String projectIdOrName, String workItemType) throws
        HttpServiceException {
        String requestSpec = String.format("/%s/%s/_apis/wit/workitemtypes/%s/states", organizationName, projectIdOrName, workItemType);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_1(requestSpec);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeStateResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public AzureArrayResponseModel<WorkItemTypeProcessStateResponseModel> getStatesForProcess(String organizationName, String processId, String workItemTypeRefName) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes/%s/states", organizationName, processId, workItemTypeRefName);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_1(requestSpec);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeProcessStateResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public WorkItemTypeProcessStateResponseModel getStatesForProcess(String organizationName, String processId, String workItemTypeRefName, String stateId) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes/%s/states/%s", organizationName, processId, workItemTypeRefName, stateId);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_1(requestSpec);
        return azureHttpService.get(requestSpec, WorkItemTypeProcessStateResponseModel.class);
    }

}
