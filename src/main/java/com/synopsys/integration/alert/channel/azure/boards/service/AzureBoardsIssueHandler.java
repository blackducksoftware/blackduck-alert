package com.synopsys.integration.alert.channel.azure.boards.service;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentLengthValidator;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueHandler;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.exception.IntegrationException;

// FIXME implement this class
public class AzureBoardsIssueHandler extends IssueHandler<WorkItemResponseModel> {
    private static final IssueContentLengthValidator CONTENT_LENGTH_VALIDATOR = new IssueContentLengthValidator(
        AzureBoardsMessageParser.TITLE_SIZE_LIMIT,
        AzureBoardsMessageParser.MESSAGE_SIZE_LIMIT,
        AzureBoardsMessageParser.MESSAGE_SIZE_LIMIT
    );

    public AzureBoardsIssueHandler() {
        super(CONTENT_LENGTH_VALIDATOR);
    }

    @Override
    protected Optional<WorkItemResponseModel> createIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        return Optional.empty();
    }

    @Override
    protected List<WorkItemResponseModel> retrieveExistingIssues(String projectSearchIdentifier, IssueTrackerRequest request) throws IntegrationException {
        return null;
    }

    @Override
    protected boolean transitionIssue(WorkItemResponseModel issueModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException {
        return false;
    }

    @Override
    protected void addComment(String issueKey, String comment) throws IntegrationException {

    }

    @Override
    protected String getIssueKey(WorkItemResponseModel issueModel) {
        return null;
    }

    @Override
    protected IssueTrackerIssueResponseModel createResponseModel(AlertIssueOrigin alertIssueOrigin, String issueTitle, IssueOperation issueOperation, WorkItemResponseModel issueResponse) {
        return null;
    }

    @Override
    protected String getIssueTrackerUrl() {
        return null;
    }

    @Override
    protected void logIssueAction(String issueTrackerProjectName, IssueTrackerRequest request) {

    }

}
