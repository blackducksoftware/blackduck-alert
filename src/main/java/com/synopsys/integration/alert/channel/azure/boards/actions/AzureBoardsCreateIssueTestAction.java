package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannel;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureTransitionHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueCreatorTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TestIssueRequestCreator;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TransitionHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;

public class AzureBoardsCreateIssueTestAction extends IssueCreatorTestAction {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCreateIssueTestAction.class);
    private final Gson gson;

    public AzureBoardsCreateIssueTestAction(AzureBoardsChannel azureBoardsChannel, Gson gson, TestIssueRequestCreator testIssueRequestCreator) {
        super(azureBoardsChannel, testIssueRequestCreator);
        this.gson = gson;
    }

    @Override
    protected String getOpenTransitionFieldKey() {

    }

    @Override
    protected String getResolveTransitionFieldKey() {
        //return AzureTransitionHandler.WORK_ITEM_STATE_CATEGORY_RESOLVED;
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
        return new AzureTransitionHandler();
    }

    @Override
    protected void safelyCleanUpIssue(IssueTrackerContext issueTrackerContext, String issueKey) {

    }
}
