/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.azure.boards;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsMessageParser;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsRequestCreator;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsRequestDelegator;
import com.synopsys.integration.alert.common.channel.IssueTrackerChannel;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsChannel extends IssueTrackerChannel {
    private final ProxyManager proxyManager;
    private final AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory;
    private final AzureBoardsRequestCreator azureBoardsRequestCreator;
    private final AzureBoardsMessageParser azureBoardsMessageParser;
    private final AzureRedirectUtil azureRedirectUtil;

    @Autowired
    public AzureBoardsChannel(Gson gson, AuditAccessor auditAccessor, AzureBoardsChannelKey channelKey, EventManager eventManager, ProxyManager proxyManager,
        AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, AzureBoardsRequestCreator azureBoardsRequestCreator, AzureBoardsMessageParser azureBoardsMessageParser,
        AzureRedirectUtil azureRedirectUtil) {
        super(gson, auditAccessor, channelKey, eventManager);
        this.proxyManager = proxyManager;
        this.credentialDataStoreFactory = credentialDataStoreFactory;
        this.azureBoardsRequestCreator = azureBoardsRequestCreator;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureRedirectUtil = azureRedirectUtil;
    }

    @Override
    protected AzureBoardsContext getIssueTrackerContext(DistributionEvent event) {
        FieldUtility fieldUtility = event.getFieldUtility();
        AzureBoardsProperties serviceConfig = AzureBoardsProperties.fromFieldAccessor(credentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), fieldUtility);
        IssueConfig issueConfig = createIssueConfig(fieldUtility);
        return new AzureBoardsContext(serviceConfig, issueConfig);
    }

    @Override
    protected List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException {
        return azureBoardsRequestCreator.createRequests(context.getIssueConfig(), event.getContent());
    }

    @Override
    public IssueTrackerResponse sendRequests(IssueTrackerContext context, List<IssueTrackerRequest> requests) throws IntegrationException {
        AzureBoardsRequestDelegator issueTrackerService = new AzureBoardsRequestDelegator(getGson(), proxyManager, (AzureBoardsContext) context, azureBoardsMessageParser);
        return issueTrackerService.sendRequests(requests);
    }

    private IssueConfig createIssueConfig(FieldUtility fieldUtility) {
        String azureProjectName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_AZURE_PROJECT);
        String workItemTypeName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE);
        boolean commentOnWorkItems = fieldUtility.getBooleanOrFalse(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT);
        String completedStateName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE);
        String reopenStateName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE);
        return new IssueConfig(
            azureProjectName,
            null,
            null,
            null,
            workItemTypeName,
            commentOnWorkItems,
            completedStateName,
            reopenStateName
        );
    }

}
