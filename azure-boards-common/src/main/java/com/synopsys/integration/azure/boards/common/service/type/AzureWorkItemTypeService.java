/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.type;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/work%20item%20types/list?view=azure-devops-rest-5.1">Documentation</a>
 */
public class AzureWorkItemTypeService {
    private final AzureHttpService azureHttpService;

    public AzureWorkItemTypeService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<WorkItemTypeResponseModel> getWorkItemTypes(String organizationName, String projectIdOrName) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/_apis/wit/workitemtypes", organizationName, projectIdOrName);
        Type responseType = new TypeToken<AzureArrayResponseModel<WorkItemTypeResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public WorkItemTypeResponseModel getWorkItemType(String organizationName, String projectIdOrName, String workItemType) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/_apis/wit/workitemtypes/%s", organizationName, projectIdOrName, workItemType);
        return azureHttpService.get(requestSpec, WorkItemTypeResponseModel.class);
    }

}
