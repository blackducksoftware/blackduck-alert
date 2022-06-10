/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.delegate;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.MessageValueReplacementResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.delegate.JiraIssueCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.server.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.ProjectService;

public class JiraServerIssueCreator extends JiraIssueCreator<IssueCreationRequestModel> {
    private final JiraServerJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator;

    public JiraServerIssueCreator(
        JiraServerChannelKey jiraServerChannelKey,
        JiraServerIssueCommenter jiraServerIssueCommenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        JiraServerJobDetailsModel distributionDetails,
        IssueService issueService,
        ProjectService projectService,
        JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        super(
            jiraServerChannelKey,
            jiraServerIssueCommenter,
            callbackInfoCreator,
            jiraErrorMessageUtility,
            issuePropertiesManager,
            JiraServerDescriptor.KEY_ISSUE_CREATOR,
            issueCategoryRetriever
        );
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
        this.projectService = projectService;
        this.jiraIssueCreationRequestCreator = jiraIssueCreationRequestCreator;
    }

    @Override
    protected IssueCreationRequestModel createIssueCreationRequest(IssueCreationModel alertIssueCreationModel, MessageReplacementValues replacementValues) throws AlertException {
        ProjectComponent jiraProject = retrieveProjectComponent();

        String issueSummary = distributionDetails.getIssueSummary();
        String title = alertIssueCreationModel.getTitle();

        if (StringUtils.isNotEmpty(issueSummary)) {
            MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(replacementValues);
            title = messageValueReplacementResolver.createReplacedFieldValue(issueSummary);
        }

        IssueRequestModelFieldsMapBuilder fieldsBuilder = jiraIssueCreationRequestCreator.createIssueRequestModel(
            title,
            alertIssueCreationModel.getDescription(),
            jiraProject.getId(),
            distributionDetails.getIssueType(),
            replacementValues,
            distributionDetails.getCustomFields()
        );
        return new IssueCreationRequestModel(
            distributionDetails.getIssueCreatorUsername(),
            distributionDetails.getIssueType(),
            distributionDetails.getProjectNameOrKey(),
            fieldsBuilder
        );
    }

    @Override
    protected IssueCreationResponseModel createIssue(IssueCreationRequestModel alertIssueCreationModel) throws IntegrationException {
        return issueService.createIssue(alertIssueCreationModel);
    }

    @Override
    protected IssueResponseModel fetchIssue(String issueIdOrKey) throws IntegrationException {
        return issueService.getIssue(issueIdOrKey);
    }

    @Override
    protected String extractReporter(IssueCreationRequestModel creationRequest) {
        return creationRequest.getReporterUsername();
    }

    private ProjectComponent retrieveProjectComponent() throws AlertException {
        String jiraProjectName = distributionDetails.getProjectNameOrKey();
        List<ProjectComponent> foundProjectComponents;
        try {
            foundProjectComponents = projectService.getProjectsByName(jiraProjectName);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to retrieve projects from Jira", e);
        }

        return foundProjectComponents
                   .stream()
                   .findAny()
                   .orElseThrow(() -> new AlertException(String.format("Unable to find project matching '%s'", jiraProjectName)));
    }

}
