/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class AzureBoardsAlertIssuePropertiesManager {
    public static final String POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL = "Policy Violated";
    public static final String CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL = "Policy";
    public static final String CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL = "Vulnerability";

    public List<WorkItemElementOperationModel> createWorkItemRequestCustomFieldOperations(ProjectIssueModel alertIssueSource) {
        LinkableItem provider = alertIssueSource.getProvider();
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(alertIssueSource.getProject());
        String subTopicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(alertIssueSource.getProjectVersion().orElse(null));

        IssueBomComponentDetails bomComponentDetails = alertIssueSource.getBomComponentDetails();
        String componentKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(bomComponentDetails.getComponent());
        String subComponentKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(bomComponentDetails.getComponentVersion().orElse(null));

        List<WorkItemElementOperationModel> customFields = new ArrayList<>(7);
        addStringField(customFields, AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, providerKey);
        addStringField(customFields, AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, topicKey);
        addStringField(customFields, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
        addStringField(customFields, AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey);
        addStringField(customFields, AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, subComponentKey);

        String categoryKey = CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL;

        Optional<String> optionalPolicyName = alertIssueSource.getPolicyDetails().map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            String additionalInfoKey = POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL + optionalPolicyName.get();
            addStringField(customFields, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey);
            categoryKey = CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL;
        }

        addStringField(customFields, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey);

        return customFields;
    }

    private void addStringField(List<WorkItemElementOperationModel> customFields, String fieldReferenceName, @Nullable String fieldValue) {
        if (null != fieldValue) {
            AzureFieldDefinition<String> alertProviderKeyFieldDefinition = AzureFieldDefinition.stringField(fieldReferenceName);
            WorkItemElementOperationModel alertProviderKeyField = WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.ADD, alertProviderKeyFieldDefinition, fieldValue);
            customFields.add(alertProviderKeyField);
        }
    }

}
