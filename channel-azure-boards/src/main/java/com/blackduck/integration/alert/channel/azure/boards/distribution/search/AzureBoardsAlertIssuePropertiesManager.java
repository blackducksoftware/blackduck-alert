/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class AzureBoardsAlertIssuePropertiesManager {
    public static final String POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL = "Policy Violated";
    public static final String CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL = "Policy";
    public static final String CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL = "Vulnerability";
    public static final String CATEGORY_TYPE_COMPONENT_UNKNOWN_VERSION_COMPATIBILITY_LABEL = "Component Unknown Version";

    public List<WorkItemElementOperationModel> createWorkItemRequestCustomFieldOperations(ProjectIssueModel alertIssueSource) {
        LinkableItem provider = alertIssueSource.getProvider();
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(alertIssueSource.getProject());
        String subTopicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(alertIssueSource.getProjectVersion().orElse(null));

        IssueBomComponentDetails bomComponentDetails = alertIssueSource.getBomComponentDetails();
        String componentKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(bomComponentDetails.getComponent());
        String subComponentKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(bomComponentDetails.getComponentVersion().orElse(null));

        AzureSearchFieldBuilder azureSearchFieldBuilder = AzureSearchFieldBuilder.create()
            .addProviderKey(providerKey)
            .addTopicKey(topicKey)
            .addSubTopicKey(subTopicKey)
            .addComponentKey(componentKey)
            .addSubComponentKey(subComponentKey);

        String categoryKey = CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL;

        Optional<String> optionalPolicyName = alertIssueSource.getPolicyDetails().map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            String additionalInfoKey = POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL + optionalPolicyName.get();
            azureSearchFieldBuilder.addAdditionalInfoKey(additionalInfoKey);
            categoryKey = CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL;
        }

        boolean unknownVersionCategory = alertIssueSource.getComponentUnknownVersionDetails().isPresent();
        if (unknownVersionCategory) {
            categoryKey = CATEGORY_TYPE_COMPONENT_UNKNOWN_VERSION_COMPATIBILITY_LABEL;
        }

        azureSearchFieldBuilder.addCategoryKey(categoryKey);

        return azureSearchFieldBuilder.build();
    }

}
