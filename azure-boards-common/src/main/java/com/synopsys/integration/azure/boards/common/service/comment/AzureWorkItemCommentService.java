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

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.comment.model.WorkItemCommentRequestModel;
import com.synopsys.integration.azure.boards.common.service.comment.model.WorkItemCommentResponseModel;
import com.synopsys.integration.azure.boards.common.service.comment.model.WorkItemMultiCommentResponseModel;

public class AzureWorkItemCommentService {
    private final AzureHttpService azureHttpService;
    private final AzureApiVersionAppender azureApiVersionAppender;

    public AzureWorkItemCommentService(AzureHttpService azureHttpService, AzureApiVersionAppender azureApiVersionAppender) {
        this.azureHttpService = azureHttpService;
        this.azureApiVersionAppender = azureApiVersionAppender;
    }

    public WorkItemMultiCommentResponseModel getComments(String organizationName, String projectIdOrName, Integer workItemId) throws HttpServiceException {
        return getComments(organizationName, projectIdOrName, workItemId, null);
    }

    public WorkItemMultiCommentResponseModel getComments(String organizationName, String projectIdOrName, Integer workItemId, @Nullable String continuationToken) throws HttpServiceException {
        String requestSpec = createCommentsSpec(organizationName, projectIdOrName, workItemId);
        if (StringUtils.isNotBlank(continuationToken)) {
            requestSpec += String.format("&continuationToken=%s", continuationToken);
        }
        return azureHttpService.get(requestSpec, WorkItemMultiCommentResponseModel.class);
    }

    public WorkItemCommentResponseModel addComment(String organizationName, String projectIdOrName, Integer workItemId, String commentText) throws HttpServiceException {
        String requestSpec = createCommentsSpec(organizationName, projectIdOrName, workItemId);
        GenericUrl requestUrl = azureHttpService.constructRequestUrl(requestSpec);
        try {
            WorkItemCommentRequestModel requestModel = new WorkItemCommentRequestModel(commentText);
            HttpRequest httpRequest = azureHttpService.buildRequestWithDefaultHeaders(HttpMethods.POST, requestUrl, requestModel);
            return azureHttpService.executeRequestAndParseResponse(httpRequest, WorkItemCommentResponseModel.class);
        } catch (IOException e) {
            throw HttpServiceException.internalServerError(e);
        }
    }

    private String createCommentsSpec(String organizationName, String projectIdOrName, Integer workItemId) {
        String spec = String.format("/%s/%s/_apis/wit/workItems/%s/comments", organizationName, projectIdOrName, workItemId);
        return azureApiVersionAppender.appendApiVersion(spec, AzureHttpService.AZURE_API_VERSION_5_1_PREVIEW_3);
    }
}
