/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.delegate;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.SearchCommentCreator;
import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCommenter;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCreator;
import com.blackduck.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesUrlCorrector;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueSearchPropertyStringCompatibilityUtils;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.exception.JiraPreconditionNotMetException;
import com.blackduck.integration.jira.common.model.components.IssueFieldsComponent;
import com.blackduck.integration.jira.common.model.response.IssueCreationResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.rest.exception.IntegrationRestException;
import org.jetbrains.annotations.Nullable;

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
        Optional<ExistingIssueDetails<String>> existingIssue = doesIssueExist(alertIssueCreationModel);
        if (existingIssue.isPresent()) {
            return existingIssue.get();
        }
        ExistingIssueDetails<String> existingIssueDetails;
        MessageReplacementValues replacementValues = alertIssueCreationModel.getSource()
            .map(this::createCustomFieldReplacementValues)
            .orElse(new MessageReplacementValues.Builder(
                alertIssueCreationModel.getProvider().getLabel(),
                MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE
            ).build());
        T creationRequest = createIssueCreationRequest(alertIssueCreationModel, replacementValues);
        try {
            IssueCreationResponseModel issueCreationResponseModel = createIssue(creationRequest);
            IssueResponseModel createdIssue = fetchIssue(issueCreationResponseModel.getKey());
            IssueFieldsComponent createdIssueFields = createdIssue.getFields();

            String issueUILink = JiraCallbackUtils.createUILink(createdIssue);

            IssueCategory issueCategory = alertIssueCreationModel.getSource()
                .map(issueCategoryRetriever::retrieveIssueCategoryFromProjectIssueModel)
                .orElse(IssueCategory.BOM);

            existingIssueDetails = new ExistingIssueDetails<>(
                createdIssue.getId(),
                    createdIssue.getKey(),
                    createdIssueFields.getSummary(),
                    issueUILink,
                    IssueStatus.RESOLVABLE,
                    issueCategory
                );
            } catch (IntegrationRestException restException) {
                throw jiraErrorMessageUtility.improveRestException(restException, issueCreatorDescriptorKey, extractReporter(creationRequest));
            } catch (JiraPreconditionNotMetException jiraException) {
                String message = StringUtils.join(FAILED_TO_CREATE_ISSUE_MESSAGE, jiraException.getMessage(), " ");
                throw new AlertException(message, jiraException);
            } catch (IntegrationException intException) {
                throw new AlertException(FAILED_TO_CREATE_ISSUE_MESSAGE, intException);
            }
        return existingIssueDetails;
    }

    @Override
    protected final void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) throws AlertException {
        JiraIssueSearchProperties searchProperties = createSearchProperties(alertIssueSource);
        issuePropertiesManager.assignIssueProperties(createdIssueDetails.getIssueKey(), searchProperties);
    }

    protected Optional<ExistingIssueDetails<String>> doesIssueExist(IssueCreationModel alertIssueCreationModel) {
        String query = alertIssueCreationModel.getQueryString().orElse(null);
        if (StringUtils.isBlank(query)) {
            return Optional.empty();
        }

        List<JiraSearcherResponseModel> response = searchForIssue(alertIssueCreationModel);
        return response.stream()
            .findFirst()
            .map(searchResponse -> convertSearchResponse(alertIssueCreationModel, searchResponse));
    }

    protected abstract List<JiraSearcherResponseModel> searchForIssue(IssueCreationModel alertIssueCreationModel);

    protected abstract T createIssueCreationRequest(IssueCreationModel alertIssueCreationModel, MessageReplacementValues replacementValues) throws AlertException;

    protected abstract IssueCreationResponseModel createIssue(T alertIssueCreationModel) throws IntegrationException;

    protected abstract IssueResponseModel fetchIssue(String createdIssueKey) throws IntegrationException;

    protected abstract String extractReporter(T creationRequest);

    private ExistingIssueDetails<String> convertSearchResponse(IssueCreationModel alertIssueCreationModel, JiraSearcherResponseModel searchResponse) {
        IssueCategory issueCategory = alertIssueCreationModel.getSource()
            .map(issueCategoryRetriever::retrieveIssueCategoryFromProjectIssueModel)
            .orElse(IssueCategory.BOM);
        String uiLink = JiraCallbackUtils.createUILink(searchResponse);
        return new ExistingIssueDetails<>(
            searchResponse.getIssueId(),
            searchResponse.getIssueKey(),
            searchResponse.getSummaryField(),
            uiLink,
            IssueStatus.RESOLVABLE,
            issueCategory
        );
    }

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

    @Override
    protected Optional<String> getAlertSearchKeys(ExistingIssueDetails<String> existingIssueDetails, @Nullable ProjectIssueModel alertIssueSource) {

        if(alertIssueSource == null) {
            return Optional.empty();
        }
        LinkableItem provider = alertIssueSource.getProvider();
        // the uuid for project and project version is the last uuid
        // use the component and version name
        // category and if policy include the policy name
        LinkableItem project = alertIssueSource.getProject();

        LinkableItem projectVersion = alertIssueSource.getProjectVersion()
                .orElseThrow(() -> new AlertRuntimeException("Missing project version"));

        IssueBomComponentDetails bomComponent = alertIssueSource.getBomComponentDetails();

        Optional<LinkableItem> componentVersion = bomComponent.getComponentVersion();
        Optional<IssuePolicyDetails> policyDetails = alertIssueSource.getPolicyDetails();
        String policyName = policyDetails.map(IssuePolicyDetails::getName).orElse(null);
        ComponentConcernType category = null;
        if (alertIssueSource.getVulnerabilityDetails().isPresent()) {
            category = ComponentConcernType.VULNERABILITY;
        } else if(policyDetails.isPresent()) {
            category = ComponentConcernType.POLICY;
        } else if(alertIssueSource.getComponentUnknownVersionDetails().isPresent()) {
            category = ComponentConcernType.UNKNOWN_VERSION;
        }
        return Optional.of(SearchCommentCreator.createSearchComment(provider, project, projectVersion, bomComponent.getComponent(), componentVersion.orElse(null),category, policyName));
    }
}
