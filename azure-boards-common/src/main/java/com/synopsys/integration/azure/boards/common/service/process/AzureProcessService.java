/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.process;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/processes/work%20item%20types/list?view=azure-devops-rest-5.1">Work Item Types</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/processes/fields/add?view=azure-devops-rest-5.1">Fields</a>
 */
public class AzureProcessService {
    private final AzureHttpService azureHttpService;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureProcessService(AzureHttpService azureHttpService, AzureApiVersionAppender azureApiVersionAppender) {
        this.azureHttpService = azureHttpService;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public AzureArrayResponseModel<ProcessWorkItemTypesResponseModel> getWorkItemTypes(String organizationName, String processId) throws HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes", organizationName, processId);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_2(requestSpec);
        Type responseType = new TypeToken<AzureArrayResponseModel<ProcessWorkItemTypesResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public ProcessWorkItemTypesResponseModel createWorkItemType(String organizationName, String processId, ProcessWorkItemTypeRequestModel requestBody) throws IOException, HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes", organizationName, processId);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_2(requestSpec);
        return azureHttpService.post(requestSpec, requestBody, ProcessWorkItemTypesResponseModel.class);
    }

    public ProcessFieldResponseModel addFieldToWorkItemType(String organizationName, String processId, String workItemTypeRefName, ProcessFieldRequestModel requestBody) throws IOException, HttpServiceException {
        String requestSpec = String.format("/%s/_apis/work/processes/%s/workItemTypes/%s/fields", organizationName, processId, workItemTypeRefName);
        requestSpec = azureApiVersionAppender.appendApiVersion5_1_Preview_2(requestSpec);
        return azureHttpService.post(requestSpec, requestBody, ProcessFieldResponseModel.class);
    }

}
