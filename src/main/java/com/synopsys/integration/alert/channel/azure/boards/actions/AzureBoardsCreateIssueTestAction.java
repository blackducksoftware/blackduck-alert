package com.synopsys.integration.alert.channel.azure.boards.actions;

import java.net.Proxy;

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
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.service.state.AzureWorkItemTypeStateService;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;

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
        return AzureTransitionHandler.WORK_ITEM_STATE_CATEGORY_IN_PROGRESS;
    }

    @Override
    protected String getDoneStatusFieldKey() {
        return AzureTransitionHandler.WORK_ITEM_STATE_CATEGORY_COMPLETED;
    }

    @Override
    protected TransitionHandler<TransitionComponent> createTransitionHandler(IssueTrackerContext issueTrackerContext) throws IntegrationException {
        //TODO:
        //need gson, DONE
        // azureBoardProperties, DONE
        // azureWorkItemService,
        // and azureWorkItemTypeStateService

        AzureBoardsProperties azureBoardsProperties = createAzureBoardProperties(issueTrackerContext);

        Proxy useProxy = proxyManager.createProxy();
        AzureHttpService azureHttpService = azureBoardsProperties.createAzureHttpService(useProxy, gson);
        AzureWorkItemService azureWorkItemService = new AzureWorkItemService(azureHttpService);

        AzureWorkItemTypeStateService azureWorkItemTypeStateService = new AzureWorkItemTypeStateService(azureHttpService);

        return new AzureTransitionHandler(gson, azureBoardsProperties, azureWorkItemService, azureWorkItemTypeStateService);
    }

    @Override
    protected void safelyCleanUpIssue(IssueTrackerContext issueTrackerContext, String issueKey) {

    }

    private AzureBoardsProperties createAzureBoardProperties(IssueTrackerContext context) {
        return (AzureBoardsProperties) context.getIssueTrackerConfig();
    }
}
