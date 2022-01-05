/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;

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
