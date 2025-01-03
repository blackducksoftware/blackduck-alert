/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageValueReplacementResolver;
import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerQueryExecutor;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.blackduck.integration.jira.common.model.response.IssueCreationResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.server.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.ProjectService;

public class JiraServerIssueCreator extends JiraIssueCreator<IssueCreationRequestModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraServerJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator;
    private JiraServerQueryExecutor jiraServerQueryExecutor;

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
        IssueCategoryRetriever issueCategoryRetriever,
        JiraServerQueryExecutor jiraServerQueryExecutor
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
        this.jiraServerQueryExecutor = jiraServerQueryExecutor;

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
    protected List<JiraSearcherResponseModel> searchForIssue(IssueCreationModel alertIssueCreationModel) {
        Optional<String> query = alertIssueCreationModel.getQueryString();
        List<JiraSearcherResponseModel> response = List.of();
        try {
            if (query.isPresent()) {
                response = jiraServerQueryExecutor.executeQuery(query.get());
            }
        } catch (AlertException ex) {
            logger.error("Query executed: {}", query);
            logger.error("Couldn't execute query to see if issue exists.", ex);
        }
        return response;
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
