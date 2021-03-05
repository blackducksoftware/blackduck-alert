/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestActionImpl;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsDistributionTestAction extends ChannelDistributionTestActionImpl {
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
        if (null == channelGlobalConfig || channelGlobalConfig.isConfiguredFieldsEmpty()) {
            throw new AlertConfigurationException("Missing Azure Boards global configuration");
        }
        DistributionJobDetailsModel distributionJobDetails = testJobModel.getDistributionJobDetails();
        AzureBoardsContext azureBoardsContext = azureBoardsContextFactory.fromConfig(channelGlobalConfig, distributionJobDetails.getAs(DistributionJobDetailsModel.AZURE));
        AzureBoardsTestIssueRequestCreator issueCreator = new AzureBoardsTestIssueRequestCreator(azureBoardsRequestCreator, azureBoardsMessageParser, customTopic, customMessage);
        AzureBoardsCreateIssueTestAction azureBoardsCreateIssueTestAction = new AzureBoardsCreateIssueTestAction((AzureBoardsChannel) getDistributionChannel(), gson, issueCreator, proxyManager);
        return azureBoardsCreateIssueTestAction.testConfig(azureBoardsContext);
    }

}
