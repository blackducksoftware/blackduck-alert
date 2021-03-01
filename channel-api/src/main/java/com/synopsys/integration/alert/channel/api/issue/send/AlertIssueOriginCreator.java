/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

@Component
public class AlertIssueOriginCreator {
    public AlertIssueOrigin createIssueOrigin(ProjectIssueModel projectIssueModel) {
        ProviderDetails providerDetails = projectIssueModel.getProviderDetails();
        LinkableItem provider = providerDetails.getProvider();

        LinkableItem project = projectIssueModel.getProject();
        LinkableItem projectVersion = projectIssueModel.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing BlackDuck project-version"));
        ContentKey providerContentKey = new ContentKey(
            provider.getLabel(),
            providerDetails.getProviderConfigId(),
            project.getLabel(),
            project.getValue(),
            projectVersion.getLabel(),
            projectVersion.getValue(),
            ItemOperation.UPDATE
        );

        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        ComponentConcernType concernType = ComponentConcernType.VULNERABILITY;
        ItemOperation operation = ItemOperation.UPDATE;
        String categoryItemName = "Unknown";

        Optional<IssuePolicyDetails> optionalPolicyDetails = projectIssueModel.getPolicyDetails();
        if (optionalPolicyDetails.isPresent()) {
            IssuePolicyDetails issuePolicyDetails = optionalPolicyDetails.get();
            concernType = ComponentConcernType.POLICY;
            operation = issuePolicyDetails.getOperation();
            categoryItemName = issuePolicyDetails.getName();
        }

        String categoryString = StringUtils.capitalize(concernType.name().toLowerCase());

        ComponentItem componentItem;
        try {
            componentItem = new ComponentItem.Builder()
                                .applyCategory(categoryString)
                                .applyOperation(operation)
                                .applyComponentData(bomComponent.getComponent())
                                .applySubComponent(bomComponent.getComponentVersion().orElse(null))
                                .applyCategoryItem(categoryString, categoryItemName)
                                .applyNotificationId(0L)
                                .build();
        } catch (AlertException e) {
            throw new AlertRuntimeException(e);
        }
        return new AlertIssueOrigin(providerContentKey, componentItem);
    }

}
