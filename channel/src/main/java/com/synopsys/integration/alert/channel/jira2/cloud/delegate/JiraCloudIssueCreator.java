/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.cloud.delegate;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.AlertIssueOriginCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.jira2.cloud.JiraCloudIssueAlertPropertiesManager;
import com.synopsys.integration.alert.channel.jira2.common.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira2.common.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.channel.jira2.common.JiraIssueSearchPropertyStringCompatibilityUtils;
import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldReplacementValues;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class JiraCloudIssueCreator extends IssueTrackerIssueCreator<String> {
    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator;
    private final JiraCloudIssueAlertPropertiesManager issuePropertiesManager;
    private final JiraErrorMessageUtility jiraErrorMessageUtility;

    public JiraCloudIssueCreator(
        JiraCloudChannelKey jiraCloudChannelKey,
        JiraCloudJobDetailsModel distributionDetails,
        IssueService issueService,
        ProjectService projectService,
        JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator,
        JiraCloudIssueCommenter jiraCloudIssueCommenter,
        JiraCloudIssueAlertPropertiesManager issuePropertiesManager,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        AlertIssueOriginCreator alertIssueOriginCreator
    ) {
        super(jiraCloudChannelKey, jiraCloudIssueCommenter, alertIssueOriginCreator);
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
        this.projectService = projectService;
        this.jiraIssueCreationRequestCreator = jiraIssueCreationRequestCreator;
        this.issuePropertiesManager = issuePropertiesManager;
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
    }

    @Override
    protected ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) throws AlertException {
        IssueCreationRequestModel creationRequest = createIssueCreationRequest(alertIssueCreationModel);
        try {
            IssueCreationResponseModel issueCreationResponseModel = issueService.createIssue(creationRequest);
            IssueResponseModel createdIssue = issueService.getIssue(issueCreationResponseModel.getKey());

            IssueFieldsComponent createdIssueFields = createdIssue.getFields();
            return new ExistingIssueDetails<>(createdIssue.getId(), createdIssue.getKey(), createdIssueFields.getSummary(), createdIssue.getSelf());
        } catch (IntegrationRestException restException) {
            throw jiraErrorMessageUtility.improveRestException(restException, JiraCloudDescriptor.KEY_ISSUE_CREATOR, creationRequest.getReporterEmail());
        } catch (IntegrationException intException) {
            throw new AlertException("Failed to create an issue in Jira.", intException);
        }
    }

    @Override
    protected void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) throws AlertException {
        JiraIssueSearchProperties searchProperties = createSearchProperties(alertIssueSource);
        issuePropertiesManager.assignIssueProperties(createdIssueDetails.getIssueKey(), searchProperties);
    }

    @Override
    protected String createUserFriendlyIssueLink(ExistingIssueDetails<String> issueDetails) {
        return JiraCallbackUtils.createUILink(issueDetails.getIssueLink(), issueDetails.getIssueKey());
    }

    private IssueCreationRequestModel createIssueCreationRequest(IssueCreationModel alertIssueCreationModel) throws AlertException {
        ProjectComponent jiraProject = retrieveProjectComponent();
        JiraCustomFieldReplacementValues replacementValues = alertIssueCreationModel.getSource()
                                                                 .map(this::createCustomFieldReplacementValues)
                                                                 .orElse(JiraCustomFieldReplacementValues.trivial(alertIssueCreationModel.getProvider()));
        IssueRequestModelFieldsMapBuilder fieldsBuilder = jiraIssueCreationRequestCreator.createIssueRequestModel(
            alertIssueCreationModel.getTitle(),
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

    private JiraIssueSearchProperties createSearchProperties(ProjectIssueModel alertIssueSource) {
        LinkableItem provider = alertIssueSource.getProvider();
        LinkableItem project = alertIssueSource.getProject();

        LinkableItem projectVersion = alertIssueSource.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing project version"));

        IssueBomComponentDetails bomComponent = alertIssueSource.getBomComponentDetails();
        LinkableItem component = bomComponent.getComponent();
        String componentVersionLabel = bomComponent.getComponentVersion().map(LinkableItem::getLabel).orElse(null);
        String componentVersionName = bomComponent.getComponentVersion().map(LinkableItem::getValue).orElse(null);

        String additionalKey = null;
        ComponentConcernType concernType = ComponentConcernType.VULNERABILITY;

        Optional<String> optionalPolicyName = alertIssueSource.getPolicyDetails().map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            concernType = ComponentConcernType.POLICY;
            additionalKey = JiraIssueSearchPropertyStringCompatibilityUtils.createPolicyAdditionalKey(optionalPolicyName.get());
        }

        String category = JiraIssueSearchPropertyStringCompatibilityUtils.createCategory(concernType);
        return new JiraIssueSearchProperties(
            provider.getLabel(),
            provider.getUrl().orElse(null),
            project.getLabel(),
            project.getValue(),
            projectVersion.getLabel(),
            projectVersion.getValue(),
            category,
            component.getLabel(),
            component.getValue(),
            componentVersionLabel,
            componentVersionName,
            additionalKey
        );
    }

    private JiraCustomFieldReplacementValues createCustomFieldReplacementValues(ProjectIssueModel alertIssueSource) {
        IssueBomComponentDetails bomComponent = alertIssueSource.getBomComponentDetails();
        return new JiraCustomFieldReplacementValues(
            alertIssueSource.getProvider().getLabel(),
            alertIssueSource.getProject().getValue(),
            alertIssueSource.getProjectVersion().map(LinkableItem::getValue).orElse(null),
            bomComponent.getComponent().getValue(),
            bomComponent.getComponentVersion().map(LinkableItem::getValue).orElse(null)
        );
    }

}
