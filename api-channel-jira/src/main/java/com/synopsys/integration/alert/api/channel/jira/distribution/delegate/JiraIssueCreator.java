/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.delegate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesUrlCorrector;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueSearchPropertyStringCompatibilityUtils;
import com.synopsys.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.exception.JiraPreconditionNotMetException;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class JiraIssueCreator<T> extends IssueTrackerIssueCreator<String> {
    private static final String FAILED_TO_CREATE_ISSUE_MESSAGE = "Failed to create an issue in Jira.";

    private final JiraErrorMessageUtility jiraErrorMessageUtility;
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;
    private final String issueCreatorDescriptorKey;

    protected JiraIssueCreator(
        IssueTrackerChannelKey channelKey,
        IssueTrackerIssueCommenter<String> commenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        String issueCreatorDescriptorKey
    ) {
        super(channelKey, commenter, callbackInfoCreator);
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
        this.issuePropertiesManager = issuePropertiesManager;
        this.issueCreatorDescriptorKey = issueCreatorDescriptorKey;
    }

    @Override
    protected final ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) throws AlertException {
        JiraCustomFieldReplacementValues replacementValues = alertIssueCreationModel.getSource()
                                                                 .map(this::createCustomFieldReplacementValues)
                                                                 .orElse(JiraCustomFieldReplacementValues.trivial(alertIssueCreationModel.getProvider()));
        T creationRequest = createIssueCreationRequest(alertIssueCreationModel, replacementValues);
        try {
            IssueCreationResponseModel issueCreationResponseModel = createIssue(creationRequest);
            IssueResponseModel createdIssue = fetchIssue(issueCreationResponseModel.getKey());
            IssueFieldsComponent createdIssueFields = createdIssue.getFields();

            String issueUILink = JiraCallbackUtils.createUILink(createdIssue);
            return new ExistingIssueDetails<>(createdIssue.getId(), createdIssue.getKey(), createdIssueFields.getSummary(), issueUILink);
        } catch (IntegrationRestException restException) {
            throw jiraErrorMessageUtility.improveRestException(restException, issueCreatorDescriptorKey, extractReporter(creationRequest));
        } catch (JiraPreconditionNotMetException jiraException) {
            String message = StringUtils.join(FAILED_TO_CREATE_ISSUE_MESSAGE, jiraException.getMessage(), " ");
            throw new AlertException(message, jiraException);
        } catch (IntegrationException intException) {
            throw new AlertException(FAILED_TO_CREATE_ISSUE_MESSAGE, intException);
        }
    }

    @Override
    protected final void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) throws AlertException {
        JiraIssueSearchProperties searchProperties = createSearchProperties(alertIssueSource);
        issuePropertiesManager.assignIssueProperties(createdIssueDetails.getIssueKey(), searchProperties);
    }

    protected abstract T createIssueCreationRequest(IssueCreationModel alertIssueCreationModel, JiraCustomFieldReplacementValues replacementValues) throws AlertException;

    protected abstract IssueCreationResponseModel createIssue(T alertIssueCreationModel) throws IntegrationException;

    protected abstract IssueResponseModel fetchIssue(String createdIssueKey) throws IntegrationException;

    protected abstract String extractReporter(T creationRequest);

    protected JiraCustomFieldReplacementValues createCustomFieldReplacementValues(ProjectIssueModel alertIssueSource) {
        IssueBomComponentDetails bomComponent = alertIssueSource.getBomComponentDetails();
        return new JiraCustomFieldReplacementValues(
            alertIssueSource.getProvider().getLabel(),
            alertIssueSource.getProject().getValue(),
            alertIssueSource.getProjectVersion().map(LinkableItem::getValue).orElse(null),
            bomComponent.getComponent().getValue(),
            bomComponent.getComponentVersion().map(LinkableItem::getValue).orElse(null)
        );
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
            provider.getUrl().flatMap(JiraIssueAlertPropertiesUrlCorrector::correctUrl).orElse(null),
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

}
