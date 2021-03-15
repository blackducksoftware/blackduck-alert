/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
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
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsChannel extends IssueTrackerChannel {
    private final Gson gson;
    private final ProxyManager proxyManager;
    private final AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory;
    private final AzureBoardsRequestCreator azureBoardsRequestCreator;
    private final AzureBoardsMessageParser azureBoardsMessageParser;
    private final AzureRedirectUtil azureRedirectUtil;

    @Autowired
    public AzureBoardsChannel(Gson gson, EventManager eventManager, ProxyManager proxyManager,
        AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory, AzureBoardsRequestCreator azureBoardsRequestCreator, AzureBoardsMessageParser azureBoardsMessageParser,
        AzureRedirectUtil azureRedirectUtil) {
        super(ChannelKeys.AZURE_BOARDS, eventManager);
        this.gson = gson;
        this.proxyManager = proxyManager;
        this.credentialDataStoreFactory = credentialDataStoreFactory;
        this.azureBoardsRequestCreator = azureBoardsRequestCreator;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureRedirectUtil = azureRedirectUtil;
    }

    @Override
    protected AzureBoardsContext getIssueTrackerContext(DistributionEvent event) throws AlertConfigurationException {
        ConfigurationModel globalConfig = event.getChannelGlobalConfig()
                                              .filter(ConfigurationModel::isConfiguredFieldsNotEmpty)
                                              .orElseThrow(() -> new AlertConfigurationException("Missing Azure Boards global configuration"));
        AzureBoardsProperties serviceConfig = AzureBoardsProperties.fromGlobalConfig(credentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), globalConfig);

        DistributionJobModel distributionJobModel = event.getDistributionJobModel();
        DistributionJobDetailsModel distributionJobDetails = distributionJobModel.getDistributionJobDetails();
        IssueConfig issueConfig = createIssueConfig(distributionJobDetails.getAs(DistributionJobDetailsModel.AZURE));
        return new AzureBoardsContext(serviceConfig, issueConfig);
    }

    @Override
    protected List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException {
        return azureBoardsRequestCreator.createRequests(context.getIssueConfig(), event.getContent());
    }

    @Override
    public IssueTrackerResponse sendRequests(IssueTrackerContext context, List<IssueTrackerRequest> requests) throws IntegrationException {
        AzureBoardsRequestDelegator issueTrackerService = new AzureBoardsRequestDelegator(gson, proxyManager, (AzureBoardsContext) context, azureBoardsMessageParser);
        return issueTrackerService.sendRequests(requests);
    }

    private IssueConfig createIssueConfig(AzureBoardsJobDetailsModel jobDetails) {
        return new IssueConfig(
            jobDetails.getProjectNameOrId(),
            null,
            null,
            null,
            jobDetails.getWorkItemType(),
            jobDetails.isAddComments(),
            jobDetails.getWorkItemCompletedState(),
            jobDetails.getWorkItemReopenState()
        );
    }

}
