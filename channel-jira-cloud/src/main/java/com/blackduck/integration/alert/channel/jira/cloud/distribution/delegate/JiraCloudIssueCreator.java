/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import java.util.List;

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
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.JiraCloudQueryExecutor;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.cloud.service.ProjectService;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.blackduck.integration.jira.common.model.response.IssueCreationResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.model.response.PageOfProjectsResponseModel;

public class JiraCloudIssueCreator extends JiraIssueCreator<IssueCreationRequestModel> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final JiraCloudQueryExecutor jiraCloudQueryExecutor;

    public JiraCloudIssueCreator(
        JiraCloudChannelKey jiraCloudChannelKey,
        JiraCloudIssueCommenter jiraCloudIssueCommenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        JiraCloudJobDetailsModel distributionDetails,
        IssueService issueService,
        ProjectService projectService,
        JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        IssueCategoryRetriever issueCategoryRetriever,
        JiraCloudQueryExecutor jiraCloudQueryExecutor
    ) {
        super(
            jiraCloudChannelKey,
            jiraCloudIssueCommenter,
            callbackInfoCreator,
            jiraErrorMessageUtility,
            issuePropertiesManager,
            JiraCloudDescriptor.KEY_ISSUE_CREATOR,
            issueCategoryRetriever
        );
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
        this.projectService = projectService;
        this.jiraIssueCreationRequestCreator = jiraIssueCreationRequestCreator;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.jiraCloudQueryExecutor = jiraCloudQueryExecutor;
    }

    @Override
    protected List<JiraSearcherResponseModel> searchForIssue(IssueCreationModel alertIssueCreationModel) {
        String query = alertIssueCreationModel.getQueryString().orElse(null);
        List<JiraSearcherResponseModel> response = List.of();
        try {
            response = jiraCloudQueryExecutor.executeQuery(query);
        } catch (AlertException ex) {
            logger.error("Query executed: {}", query);
            logger.error("Couldn't execute query to see if issue exists.", ex);
        }
        return response;
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
            distributionDetails.getIssueCreatorEmail(),
            distributionDetails.getIssueType(),
            distributionDetails.getProjectNameOrKey(),
            fieldsBuilder,
            List.of()
        );
    }

    @Override
    protected IssueCreationResponseModel createIssue(IssueCreationRequestModel alertIssueCreationModel) throws IntegrationException {
        return issueService.createIssue(alertIssueCreationModel);
    }

    @Override
    protected IssueResponseModel fetchIssue(String issueKeyOrId) throws IntegrationException {
        return issueService.getIssue(issueKeyOrId);
    }

    @Override
    protected String extractReporter(IssueCreationRequestModel creationRequest) {
        return creationRequest.getReporterEmail();
    }

    private ProjectComponent retrieveProjectComponent() throws AlertException {
        String jiraProjectName = distributionDetails.getProjectNameOrKey();
        PageOfProjectsResponseModel projectsResponseModel;
        try {
            projectsResponseModel = projectService.getProjectsByName(jiraProjectName);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to retrieve projects from Jira", e);
        }
        return projectsResponseModel.getProjects()
            .stream()
            .filter(project -> jiraProjectName.equals(project.getName()) || jiraProjectName.equals(project.getKey()))
            .findAny()
            .orElseThrow(() -> new AlertException(String.format("Unable to find project matching '%s'", jiraProjectName)));
    }

}
