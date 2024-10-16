/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class AzureBoardsAlertIssuePropertiesManagerTest {

    @Test
    void verifyValidVulnIssuesAreCreated() {
        AzureBoardsAlertIssuePropertiesManager azureBoardsAlertIssuePropertiesManager = new AzureBoardsAlertIssuePropertiesManager();
        LinkableItem providerConfig = new LinkableItem("providerConfigLabel", "providerConfigValue");
        ProviderDetails providerDetails = new ProviderDetails(1L, providerConfig);
        LinkableItem project = new LinkableItem("projectLabel", "projectValue");
        LinkableItem projectVersion = new LinkableItem("projectVersionLabel", "projectVersionValue");
        LinkableItem component = new LinkableItem("componentLabel", "componentValue");
        LinkableItem componentVersion = new LinkableItem("componentVersionLabel", "componentVersionValue");
        IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromSearchResults(component, componentVersion);
        IssueVulnerabilityDetails issueVulnerabilityDetails = new IssueVulnerabilityDetails(
            false,
            List.of(IssueVulnerabilityModel.fromComponentConcern(ComponentConcern.vulnerability(
                ItemOperation.ADD,
                "vulnerabilityId",
                ComponentConcernSeverity.CRITICAL,
                "vulnerabilityUrl"))
            ),
            List.of(),
            List.of()
        );
        ProjectIssueModel vulnerability = ProjectIssueModel.vulnerability(
            providerDetails,
            project,
            projectVersion,
            issueBomComponentDetails,
            issueVulnerabilityDetails
        );
        List<WorkItemElementOperationModel> workItemRequestCustomFieldOperations = azureBoardsAlertIssuePropertiesManager.createWorkItemRequestCustomFieldOperations(vulnerability);

        LinkableItem provider = providerDetails.getProvider();
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, providerKey);

        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL);

        String subTopicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(vulnerability.getProjectVersion().orElse(null));
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
    }

    @Test
    void verifyValidPolicyIssuesAreCreated() {
        AzureBoardsAlertIssuePropertiesManager azureBoardsAlertIssuePropertiesManager = new AzureBoardsAlertIssuePropertiesManager();
        LinkableItem providerConfig = new LinkableItem("providerConfigLabel", "providerConfigValue");
        ProviderDetails providerDetails = new ProviderDetails(1L, providerConfig);
        LinkableItem project = new LinkableItem("projectLabel", "projectValue");
        LinkableItem projectVersion = new LinkableItem("projectVersionLabel", "projectVersionValue");
        LinkableItem component = new LinkableItem("componentLabel", "componentValue");
        LinkableItem componentVersion = new LinkableItem("componentVersionLabel", "componentVersionValue");
        IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromSearchResults(component, componentVersion);
        IssuePolicyDetails issuePolicyDetails = new IssuePolicyDetails("policy", ItemOperation.ADD, ComponentConcernSeverity.CRITICAL);
        ProjectIssueModel policy = ProjectIssueModel.policy(
            providerDetails,
            project,
            projectVersion,
            issueBomComponentDetails,
            issuePolicyDetails
        );
        List<WorkItemElementOperationModel> workItemRequestCustomFieldOperations = azureBoardsAlertIssuePropertiesManager.createWorkItemRequestCustomFieldOperations(policy);

        LinkableItem provider = providerDetails.getProvider();
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, providerKey);

        String additionalInfo = AzureBoardsAlertIssuePropertiesManager.POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL + issuePolicyDetails.getName();
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfo);

        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL);

        String subTopicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(policy.getProjectVersion().orElse(null));
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
    }

    @Test
    void verifyPolicyIssuesAreCreatedWithTruncatedPropertyValues() {
        AzureBoardsAlertIssuePropertiesManager azureBoardsAlertIssuePropertiesManager = new AzureBoardsAlertIssuePropertiesManager();
        LinkableItem providerConfig = new LinkableItem("providerConfigLabel", "providerConfigValue");
        ProviderDetails providerDetails = new ProviderDetails(1L, providerConfig);
        LinkableItem project = new LinkableItem("projectLabel", "projectValue");
        LinkableItem projectVersion = new LinkableItem("projectVersionLabel", StringUtils.repeat("projectVersionValue", 100));
        LinkableItem component = new LinkableItem("componentLabel", "componentValue");
        LinkableItem componentVersion = new LinkableItem("componentVersionLabel", "componentVersionValue");
        IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromSearchResults(component, componentVersion);
        IssuePolicyDetails issuePolicyDetails = new IssuePolicyDetails("policy", ItemOperation.ADD, ComponentConcernSeverity.CRITICAL);
        ProjectIssueModel policy = ProjectIssueModel.policy(
            providerDetails,
            project,
            projectVersion,
            issueBomComponentDetails,
            issuePolicyDetails
        );
        List<WorkItemElementOperationModel> workItemRequestCustomFieldOperations = azureBoardsAlertIssuePropertiesManager.createWorkItemRequestCustomFieldOperations(policy);

        LinkableItem provider = providerDetails.getProvider();
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, providerKey);

        String additionalInfo = AzureBoardsAlertIssuePropertiesManager.POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL + issuePolicyDetails.getName();
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfo);

        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL);

        // This should trim the contents of the linkableItem
        String subTopicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(policy.getProjectVersion().orElse(null));
        assertValidContents(workItemRequestCustomFieldOperations, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
        WorkItemElementOperationModel workItem = workItemRequestCustomFieldOperations.stream()
            .filter(workItemElementOperationModel -> StringUtils.endsWith(workItemElementOperationModel.getPath(), AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME))
            .findFirst()
            .orElse(null);
        assertNotNull(workItem);
        Object workItemValue = workItem.getValue();
        assertEquals(subTopicKey, workItemValue);
        // Ensures that we don't push values that are larger than azure allows for custom fields
        assertEquals(AzureBoardsSearchPropertiesUtils.MAX_STRING_VALUE_LENGTH, workItemValue.toString().length());
    }

    private void assertValidContents(List<WorkItemElementOperationModel> workItems, String searchKey, String expectedValue) {
        WorkItemElementOperationModel workItem = workItems.stream()
            .filter(workItemElementOperationModel -> StringUtils.endsWith(workItemElementOperationModel.getPath(), searchKey))
            .findFirst()
            .orElse(null);

        assertNotNull(workItem);

        Object workItemValue = workItem.getValue();
        assertEquals(expectedValue, workItemValue);
    }
}
