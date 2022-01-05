/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.delegate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesUrlCorrector;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueSearchPropertyStringCompatibilityUtils;
import com.synopsys.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
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
    private final IssueCategoryRetriever issueCategoryRetriever;

    protected JiraIssueCreator(
        IssueTrackerChannelKey channelKey,
        IssueTrackerIssueCommenter<String> commenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        String issueCreatorDescriptorKey,
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        super(channelKey, commenter, callbackInfoCreator);
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
        this.issuePropertiesManager = issuePropertiesManager;
        this.issueCreatorDescriptorKey = issueCreatorDescriptorKey;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    @Override
    protected final ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) throws AlertException {
        MessageReplacementValues replacementValues = alertIssueCreationModel.getSource()
                                                         .map(this::createCustomFieldReplacementValues)
                                                         .orElse(new MessageReplacementValues.Builder(alertIssueCreationModel.getProvider().getLabel(), MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE).build());
        T creationRequest = createIssueCreationRequest(alertIssueCreationModel, replacementValues);
        try {
            IssueCreationResponseModel issueCreationResponseModel = createIssue(creationRequest);
            IssueResponseModel createdIssue = fetchIssue(issueCreationResponseModel.getKey());
            IssueFieldsComponent createdIssueFields = createdIssue.getFields();

            String issueUILink = JiraCallbackUtils.createUILink(createdIssue);

            IssueCategory issueCategory = alertIssueCreationModel.getSource()
                                              .map(issueCategoryRetriever::retrieveIssueCategoryFromProjectIssueModel)
                                              .orElse(IssueCategory.BOM);

            return new ExistingIssueDetails<>(createdIssue.getId(), createdIssue.getKey(), createdIssueFields.getSummary(), issueUILink, IssueStatus.RESOLVABLE, issueCategory);
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

    protected abstract T createIssueCreationRequest(IssueCreationModel alertIssueCreationModel, MessageReplacementValues replacementValues) throws AlertException;

    protected abstract IssueCreationResponseModel createIssue(T alertIssueCreationModel) throws IntegrationException;

    protected abstract IssueResponseModel fetchIssue(String createdIssueKey) throws IntegrationException;

    protected abstract String extractReporter(T creationRequest);

    protected MessageReplacementValues createCustomFieldReplacementValues(ProjectIssueModel alertIssueSource) {
        IssueBomComponentDetails bomComponent = alertIssueSource.getBomComponentDetails();
        ComponentUpgradeGuidance upgradeGuidance = bomComponent.getComponentUpgradeGuidance();

        Optional<String> severity = Optional.empty();
        Optional<String> policyCategory = Optional.empty();
        Optional<IssuePolicyDetails> issuePolicyDetails = alertIssueSource.getPolicyDetails();
        Optional<IssueVulnerabilityDetails> vulnerabilityDetails = alertIssueSource.getVulnerabilityDetails();
        if (issuePolicyDetails.isPresent()) {
            IssuePolicyDetails policyDetails = issuePolicyDetails.get();
            severity = Optional.ofNullable(policyDetails.getSeverity().getPolicyLabel());
            policyCategory = bomComponent.getRelevantPolicies()
                                 .stream()
                                 .filter(policy -> policyDetails.getName().equals(policy.getPolicyName()))
                                 .findAny()
                                 .flatMap(ComponentPolicy::getCategory);
        }
        if (vulnerabilityDetails.isPresent()) {
            severity = vulnerabilityDetails.get().getHighestSeverityAddedOrUpdated();
        }
        return new MessageReplacementValues.Builder(alertIssueSource.getProvider().getLabel(), alertIssueSource.getProject().getValue())
                   .projectVersionName(alertIssueSource.getProjectVersion().map(LinkableItem::getValue).orElse(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE))
                   .componentName(bomComponent.getComponent().getValue())
                   .componentVersionName(bomComponent.getComponentVersion().map(LinkableItem::getValue).orElse(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE))
                   .componentUsage(bomComponent.getUsage())
                   .componentLicense(bomComponent.getLicense().getValue())
                   .severity(severity.orElse(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE))
                   .policyCategory(policyCategory.orElse(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE))
                   .shortTermUpgradeGuidance(upgradeGuidance.getShortTermUpgradeGuidance().map(LinkableItem::getValue).orElse(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE))
                   .longTermUpgradeGuidance(upgradeGuidance.getLongTermUpgradeGuidance().map(LinkableItem::getValue).orElse(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE))
                   .build();
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

        if (alertIssueSource.getComponentUnknownVersionDetails().isPresent()) {
            concernType = ComponentConcernType.UNKNOWN_VERSION;
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
