package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannel;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsContext;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsMessageParser;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsDistributionTestAction extends ChannelDistributionTestAction {
    private final Gson gson;
    private final AzureBoardsMessageParser azureBoardsMessageParser;

    @Autowired
    public AzureBoardsDistributionTestAction(AzureBoardsChannel azureBoardsChannel, Gson gson, AzureBoardsMessageParser azureBoardsMessageParser) {
        //TODO: See if I need set up anything else, GSON, Parser?
        super(azureBoardsChannel);
        this.gson = gson;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
    }

    @Override
    public MessageResult testConfig(String jobId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        //TODO: test the config here
        AzureBoardsContext azureBoardsContext = new AzureBoardsContext();

        //Need a testAction object like JiraCloudCreateIssueTestAction
        //AzureBoardsCreateIssueTestAction
        return;
    }
}
