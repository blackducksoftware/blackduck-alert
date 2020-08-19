package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannel;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsContext;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsContextFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsTestIssueRequestCreator;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsMessageParser;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsDistributionTestAction extends ChannelDistributionTestAction {
    private final Gson gson;
    private final AzureBoardsMessageParser azureBoardsMessageParser;
    private final AzureBoardsContextFactory azureBoardsContextFactory;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsDistributionTestAction(AzureBoardsChannel azureBoardsChannel, Gson gson, AzureBoardsMessageParser azureBoardsMessageParser, AzureBoardsContextFactory azureBoardsContextFactory, ProxyManager proxyManager) {
        super(azureBoardsChannel);
        this.gson = gson;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
        this.azureBoardsContextFactory = azureBoardsContextFactory;
        this.proxyManager = proxyManager;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        AzureBoardsContext azureBoardsContext = azureBoardsContextFactory.build(registeredFieldValues);
        AzureBoardsTestIssueRequestCreator issueCreator = new AzureBoardsTestIssueRequestCreator(registeredFieldValues, azureBoardsMessageParser);
        AzureBoardsCreateIssueTestAction azureBoardsCreateIssueTestAction = new AzureBoardsCreateIssueTestAction((AzureBoardsChannel) getDistributionChannel(), gson, issueCreator, proxyManager);
        return azureBoardsCreateIssueTestAction.testConfig(azureBoardsContext);
    }
}
