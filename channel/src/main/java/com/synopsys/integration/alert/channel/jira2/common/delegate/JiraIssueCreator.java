package com.synopsys.integration.alert.channel.jira2.common.delegate;

import java.util.Optional;

import com.synopsys.integration.alert.channel.api.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.jira2.cloud.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.channel.jira2.common.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira2.common.JiraIssueSearchPropertyStringCompatibilityUtils;
import com.synopsys.integration.alert.channel.jira2.common.model.JiraCustomFieldReplacementValues;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class JiraIssueCreator<T> extends IssueTrackerIssueCreator<String> {
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
        T creationRequest = createIssueCreationRequest(alertIssueCreationModel);
        try {
            IssueCreationResponseModel issueCreationResponseModel = createIssue(creationRequest);
            IssueResponseModel createdIssue = fetchIssue(issueCreationResponseModel.getKey());
            IssueFieldsComponent createdIssueFields = createdIssue.getFields();

            String issueUILink = JiraCallbackUtils.createUILink(createdIssue);
            return new ExistingIssueDetails<>(createdIssue.getId(), createdIssue.getKey(), createdIssueFields.getSummary(), issueUILink);
        } catch (IntegrationRestException restException) {
            throw jiraErrorMessageUtility.improveRestException(restException, issueCreatorDescriptorKey, getReporter(creationRequest));
        } catch (IntegrationException intException) {
            throw new AlertException("Failed to create an issue in Jira.", intException);
        }
    }

    @Override
    protected final void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) throws AlertException {
        JiraIssueSearchProperties searchProperties = createSearchProperties(alertIssueSource);
        issuePropertiesManager.assignIssueProperties(createdIssueDetails.getIssueKey(), searchProperties);
    }

    protected abstract T createIssueCreationRequest(IssueCreationModel alertIssueCreationModel) throws AlertException;

    protected abstract IssueCreationResponseModel createIssue(T alertIssueCreationModel) throws IntegrationException;

    protected abstract IssueResponseModel fetchIssue(String createdIssueKey) throws IntegrationException;

    protected abstract String getReporter(T creationRequest);

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

}
