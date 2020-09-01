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
package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannel;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureTransitionHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueCreatorTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TestIssueRequestCreator;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TransitionHandler;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class AzureBoardsCreateIssueTestAction extends IssueCreatorTestAction {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCreateIssueTestAction.class);
    private final Gson gson;
    private final ProxyManager proxyManager;

    public AzureBoardsCreateIssueTestAction(AzureBoardsChannel azureBoardsChannel, Gson gson, TestIssueRequestCreator testIssueRequestCreator, ProxyManager proxyManager) {
        super(azureBoardsChannel, testIssueRequestCreator);
        this.gson = gson;
        this.proxyManager = proxyManager;
    }

    @Override
    protected String getOpenTransitionFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE;
    }

    @Override
    protected String getTodoStatusFieldKey() {
        return AzureTransitionHandler.WORK_ITEM_STATE_CATEGORY_PROPOSED;
    }

    @Override
    protected String getDoneStatusFieldKey() {
        return AzureTransitionHandler.WORK_ITEM_STATE_CATEGORY_COMPLETED;
    }

    @Override
    protected TransitionHandler<WorkItemTypeStateResponseModel> createTransitionHandler(IssueTrackerContext issueTrackerContext) throws IntegrationException {
        AzureBoardsProperties azureBoardsProperties = createAzureBoardProperties(issueTrackerContext);
        AzureHttpService azureHttpService = createAzureHttpService(azureBoardsProperties);
        AzureWorkItemService azureWorkItemService = new AzureWorkItemService(azureHttpService);
        AzureWorkItemTypeStateService azureWorkItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService, new AzureApiVersionAppender());
        return new AzureTransitionHandler(gson, azureBoardsProperties, azureWorkItemService, azureWorkItemTypeStateService);
    }

    @Override
    protected void safelyCleanUpIssue(IssueTrackerContext issueTrackerContext, String issueKey) {
        try {
            AzureBoardsProperties azureBoardsProperties = createAzureBoardProperties(issueTrackerContext);
            AzureHttpService azureHttpService = createAzureHttpService(azureBoardsProperties);
            AzureWorkItemService azureWorkItemService = new AzureWorkItemService(azureHttpService);
            Integer workItemId = Integer.parseInt(issueKey);
            azureWorkItemService.deleteWorkItem(azureBoardsProperties.getOrganizationName(), workItemId);
        } catch (IntegrationException e) {
            logger.warn(String.format("There was a problem trying to delete the Azure Boards distribution test issue, '%s': %s", issueKey, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
    }

    private AzureBoardsProperties createAzureBoardProperties(IssueTrackerContext context) {
        return (AzureBoardsProperties) context.getIssueTrackerConfig();
    }

    private AzureHttpService createAzureHttpService(AzureBoardsProperties azureBoardsProperties) throws IntegrationException {
        ProxyInfo proxy = proxyManager.createProxyInfo();
        return azureBoardsProperties.createAzureHttpService(proxy, gson);
    }

}
