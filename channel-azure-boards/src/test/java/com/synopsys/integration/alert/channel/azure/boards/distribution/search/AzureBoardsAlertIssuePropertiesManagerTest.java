package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;

public class AzureBoardsAlertIssuePropertiesManagerTest {

    @Test
    public void verifyValidVulnIssuesAreCreated() {
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
    public void verifyValidPolicyIssuesAreCreated() {
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
    public void verifyPolicyIssuesAreCreatedWithTruncatedPropertyValues() {
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

        // This should trim the contents of the linkableitem
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
        assertEquals(256, workItemValue.toString().length());
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
