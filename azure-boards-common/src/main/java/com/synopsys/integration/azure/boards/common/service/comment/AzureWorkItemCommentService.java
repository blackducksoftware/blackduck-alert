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
package com.synopsys.integration.azure.boards.common.service.comment;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemCommentResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemMultiCommentResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

public class AzureWorkItemCommentService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_COMMENTS = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workItems/{workItemId}/comments");

    public static final String PATH_ORGANIZATION_REPLACEMENT = "{organization}";
    public static final String PATH_PROJECT_REPLACEMENT = "{project}";
    public static final String PATH_WORK_ITEM_ID_REPLACEMENT = "{workItemId}";
    public static final String API_VERSION_PREVIEW_3 = "5.1-preview.3";

    private final AzureHttpService azureHttpService;

    public AzureWorkItemCommentService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public WorkItemMultiCommentResponseModel getComments(String organizationName, String projectIdOrName, Integer workItemId) throws HttpServiceException {
        return getComments(organizationName, projectIdOrName, workItemId, null);
    }

    public WorkItemMultiCommentResponseModel getComments(String organizationName, String projectIdOrName, Integer workItemId, @Nullable String continuationToken) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_COMMENTS
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_WORK_ITEM_ID_REPLACEMENT, workItemId.toString())
                                 .populateSpec();
        String apiVersionQueryParam = new AzureSpecTemplate("?{apiVersionParam}={apiVersion}")
                                          .defineReplacement("{apiVersionParam}", AzureHttpService.AZURE_API_VERSION_QUERY_PARAM_NAME)
                                          .defineReplacement("{apiVersion}", API_VERSION_PREVIEW_3)
                                          .populateSpec();
        requestSpec = requestSpec + apiVersionQueryParam;
        if (StringUtils.isNotBlank(continuationToken)) {
            String continuationTokenQueryParam = new AzureSpecTemplate("&continuationToken={continuationToken}")
                                                     .defineReplacement("{continuationToken}", continuationToken)
                                                     .populateSpec();
            requestSpec = requestSpec + continuationTokenQueryParam;
        }
        return azureHttpService.get(requestSpec, WorkItemMultiCommentResponseModel.class);
    }

    public WorkItemCommentResponseModel addComment(String organizationName, String projectIdOrName, Integer workItemId, String commentText) throws HttpServiceException {
        return addComment(organizationName, projectIdOrName, workItemId, List.of(commentText));
    }

    public WorkItemCommentResponseModel addComment(String organizationName, String projectIdOrName, Integer workItemId, List<String> commentTexts) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_COMMENTS
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_REPLACEMENT, projectIdOrName)
                                 .defineReplacement(PATH_WORK_ITEM_ID_REPLACEMENT, workItemId.toString())
                                 .populateSpec();
        requestSpec = String.format("%s?%s=%s", requestSpec, AzureHttpService.AZURE_API_VERSION_QUERY_PARAM_NAME, API_VERSION_PREVIEW_3);
        GenericUrl requestUrl = azureHttpService.constructRequestUrl(requestSpec);
        try {
            HttpRequest httpRequest = azureHttpService.buildRequestWithDefaultHeaders(HttpMethods.POST, requestUrl, commentTexts);
            return azureHttpService.executeRequestAndParseResponse(httpRequest, WorkItemCommentResponseModel.class);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

}
