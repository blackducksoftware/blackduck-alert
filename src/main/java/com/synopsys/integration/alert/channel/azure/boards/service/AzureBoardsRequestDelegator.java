/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.azure.boards.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsContext;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentLengthValidator;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.synopsys.integration.azure.boards.common.service.process.AzureProcessService;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.exception.IntegrationException;

public class AzureBoardsRequestDelegator {
    private final Gson gson;
    private final ProxyManager proxyManager;
    private final AzureBoardsContext context;
    private final AzureBoardsMessageParser azureBoardsMessageParser;

    public AzureBoardsRequestDelegator(Gson gson, ProxyManager proxyManager, AzureBoardsContext context, AzureBoardsMessageParser azureBoardsMessageParser) {
        this.gson = gson;
        this.proxyManager = proxyManager;
        this.context = context;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
    }

    public IssueTrackerResponse sendRequests(List<IssueTrackerRequest> requests) throws IntegrationException {
        IssueConfig azureIssueConfig = context.getIssueConfig();
        AzureBoardsProperties azureBoardsProperties = context.getIssueTrackerConfig();
        azureBoardsProperties.validateProperties();

        HttpTransport httpTransport = azureBoardsProperties.createHttpTransport(proxyManager.createProxyInfo());
        Credential oAuthCredential = retrieveOAuthCredential(azureBoardsProperties, httpTransport);
        AzureHttpService azureHttpService = AzureHttpServiceFactory.withCredential(httpTransport, oAuthCredential, gson);


        AzureApiVersionAppender azureApiVersionAppender = new AzureApiVersionAppender();
        AzureProjectService azureProjectService = new AzureProjectService(azureHttpService, azureApiVersionAppender);
        AzureProcessService azureProcessService = new AzureProcessService(azureHttpService, azureApiVersionAppender);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AzureCustomFieldManager azureCustomFieldInstaller =
            new AzureCustomFieldManager(azureBoardsProperties.getOrganizationName(), azureProjectService, azureProcessService, executorService);
        try {
            azureCustomFieldInstaller.installCustomFields(azureIssueConfig.getProjectName(), azureIssueConfig.getIssueType());
        } finally {
            executorService.isShutdown();
        }

        IssueContentLengthValidator workItemContentLengthValidator =
            new IssueContentLengthValidator(AzureBoardsMessageParser.TITLE_SIZE_LIMIT, AzureBoardsMessageParser.MESSAGE_SIZE_LIMIT, AzureBoardsMessageParser.MESSAGE_SIZE_LIMIT);
        AzureWorkItemService azureWorkItemService = new AzureWorkItemService(azureHttpService);
        AzureWorkItemQueryService azureWorkItemQueryService = new AzureWorkItemQueryService(azureHttpService, azureApiVersionAppender);
        AzureWorkItemCommentService azureWorkItemCommentService = new AzureWorkItemCommentService(azureHttpService, azureApiVersionAppender);
        AzureBoardsIssueHandler issueHandler = new AzureBoardsIssueHandler(workItemContentLengthValidator, azureBoardsProperties, azureBoardsMessageParser, azureWorkItemService, azureWorkItemCommentService, azureWorkItemQueryService);
        return issueHandler.createOrUpdateIssues(azureIssueConfig, requests);
    }

    private Credential retrieveOAuthCredential(AzureBoardsProperties azureBoardsProperties, HttpTransport httpTransport) throws IntegrationException {
        try {
            AuthorizationCodeFlow oAuthFlow = azureBoardsProperties.createOAuthFlow(httpTransport);
            return azureBoardsProperties.getExistingOAuthCredential(oAuthFlow)
                       .orElseThrow(() -> new AlertException("No stored OAuth credential for Azure Boards exists"));
        } catch (IOException e) {
            throw new IntegrationException("Cannot initialize OAuth for Azure Boards", e);
        }
    }

}
