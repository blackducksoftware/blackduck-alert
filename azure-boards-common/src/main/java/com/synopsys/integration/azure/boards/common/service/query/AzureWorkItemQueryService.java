/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query;

import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/wiql?view=azure-devops-rest-5.1">Documentation</a>
 */
public class AzureWorkItemQueryService {
    private final AzureHttpService azureHttpService;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureWorkItemQueryService(AzureHttpService azureHttpService, AzureApiVersionAppender azureApiVersionAppender) {
        this.azureHttpService = azureHttpService;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public WorkItemQueryResultResponseModel queryForWorkItems(String organizationName, String projectIdOrName, WorkItemQuery query) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/_apis/wit/wiql", organizationName, projectIdOrName);
        requestSpec = azureApiVersionAppender.appendApiVersion5_0(requestSpec);
        WorkItemQueryRequestModel requestModel = new WorkItemQueryRequestModel(query.rawQuery());
        return azureHttpService.post(requestSpec, requestModel, WorkItemQueryResultResponseModel.class);
    }

}
