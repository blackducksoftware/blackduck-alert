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
import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

public class AzureWorkItemService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitems/{workitemId}");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_TYPE = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitems/{type}");

    private final AzureHttpService azureHttpService;

    public AzureWorkItemService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public WorkItemResponseModel getWorkItem(String organizationName, String projectIdOrName, Integer workItemId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{project}", projectIdOrName)
                                 .defineReplacement("{workitemId}", workItemId.toString())
                                 .populateSpec();
        return azureHttpService.get(requestSpec, WorkItemResponseModel.class);
    }

    public WorkItemResponseModel createWorkItem(String organizationName, String projectIdOrName, String workItemType, List<WorkItemElementRequestModel> requestModel) throws HttpServiceException, IOException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_TYPE
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{project}", projectIdOrName)
                                 .defineReplacement("{type}", workItemType)
                                 .populateSpec();
        HttpRequest httpRequest = buildRequest(requestSpec, requestModel);
        return azureHttpService.post(httpRequest, WorkItemResponseModel.class);
    }

    private HttpRequest buildRequest(String requestSpec, List<WorkItemElementRequestModel> requestModel) throws IOException {
        GenericUrl requestUrl = azureHttpService.constructRequestUrl(requestSpec);
        HttpRequest httpRequest = azureHttpService.buildPostRequest(requestUrl, requestModel);
        httpRequest.getHeaders().setContentType("application/json-patch+json");
        return httpRequest;
    }

}
