/*
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
package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannel;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsContext;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsContextFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsTestIssueRequestCreator;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsMessageParser;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsRequestCreator;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsDistributionTestAction extends ChannelDistributionTestAction {
    private final Gson gson;
    private final AzureBoardsRequestCreator azureBoardsRequestCreator;
    private final AzureBoardsMessageParser azureBoardsMessageParser;
    private final AzureBoardsContextFactory azureBoardsContextFactory;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsDistributionTestAction(AzureBoardsChannel azureBoardsChannel, Gson gson, AzureBoardsRequestCreator azureBoardsRequestCreator,
        AzureBoardsMessageParser azureBoardsMessageParser, AzureBoardsContextFactory azureBoardsContextFactory, ProxyManager proxyManager) {
        super(azureBoardsChannel);
        this.gson = gson;
        this.azureBoardsRequestCreator = azureBoardsRequestCreator;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureBoardsContextFactory = azureBoardsContextFactory;
        this.proxyManager = proxyManager;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        AzureBoardsContext azureBoardsContext = azureBoardsContextFactory.build(registeredFieldValues);
        AzureBoardsTestIssueRequestCreator issueCreator = new AzureBoardsTestIssueRequestCreator(registeredFieldValues, azureBoardsRequestCreator, azureBoardsMessageParser);
        AzureBoardsCreateIssueTestAction azureBoardsCreateIssueTestAction = new AzureBoardsCreateIssueTestAction((AzureBoardsChannel) getDistributionChannel(), gson, issueCreator, proxyManager);
        return azureBoardsCreateIssueTestAction.testConfig(azureBoardsContext);
    }
}
