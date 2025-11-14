/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
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
import com.blackduck.integration.jira.common.cloud.builder.AtlassianDocumentFormatModelBuilder;
import com.blackduck.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import com.blackduck.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.cloud.service.ProjectService;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.blackduck.integration.jira.common.model.response.IssueCreationResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.model.response.PageOfProjectsResponseModel;

public class JiraCloudIssueCreator extends JiraIssueCreator<IssueCreationRequestModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final JiraCloudQueryExecutor jiraCloudQueryExecutor;
    private final JiraCloudIssueCommenter jiraCloudIssueCommenter;

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
        this.jiraCloudIssueCommenter = jiraCloudIssueCommenter;
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

        // The description field is an empty string.  The actual description comes from the atlassian model.  Set the description field.
        Optional<AtlassianDocumentFormatModel> description = alertIssueCreationModel.getAtlassianDocumentFormatDescriptionModel();
        description.ifPresent(atlassianDocumentFormatModel -> fieldsBuilder.setField(IssueRequestModelFieldsBuilder.DESCRIPTION, atlassianDocumentFormatModel));

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

    @Override
    protected void addPostCreateComments(ExistingIssueDetails<String> issueDetails, IssueCreationModel creationModel, @Nullable ProjectIssueModel projectSource)
        throws AlertException {
        LinkedList<AtlassianDocumentFormatModel> postCreateComments = new LinkedList<>();
        AtlassianDocumentFormatModelBuilder atlassianDocumentFormatModelBuilder = new AtlassianDocumentFormatModelBuilder();
        atlassianDocumentFormatModelBuilder.addSingleParagraphTextNode("This issue was automatically created by Alert.");
        AtlassianDocumentFormatModel firstComment = atlassianDocumentFormatModelBuilder.build();
        Optional<List<AtlassianDocumentFormatModel>> additionalCommentList = creationModel.getAtlassianDocumentFormatCommentModel();
        additionalCommentList.ifPresent(postCreateComments::addAll);

        IssueCommentModel<String> commentRequestModel = new IssueCommentModel<>(issueDetails, List.of(), projectSource, firstComment, postCreateComments);
        jiraCloudIssueCommenter.commentOnIssue(commentRequestModel);
    }

}
