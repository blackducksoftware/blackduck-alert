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
package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.jetbrains.annotations.Nullable;
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
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
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
    public MessageResult testConfig(
        DistributionJobModel testJobModel,
        @Nullable ConfigurationModel channelGlobalConfig,
        @Nullable String customTopic,
        @Nullable String customMessage,
        @Nullable String destination
    ) throws IntegrationException {
        DistributionJobDetailsModel distributionJobDetails = testJobModel.getDistributionJobDetails();
        AzureBoardsContext azureBoardsContext = azureBoardsContextFactory.fromConfig(channelGlobalConfig, distributionJobDetails.getAs(DistributionJobDetailsModel.AZURE));
        AzureBoardsTestIssueRequestCreator issueCreator = new AzureBoardsTestIssueRequestCreator(azureBoardsRequestCreator, azureBoardsMessageParser, customTopic, customMessage);
        AzureBoardsCreateIssueTestAction azureBoardsCreateIssueTestAction = new AzureBoardsCreateIssueTestAction((AzureBoardsChannel) getDistributionChannel(), gson, issueCreator, proxyManager);
        return azureBoardsCreateIssueTestAction.testConfig(azureBoardsContext);
    }

}
