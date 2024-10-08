/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractor;
import com.blackduck.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
import com.blackduck.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;
import com.blackduck.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.RuleViolationNotificationView;

@Component
public class RuleViolationNotificationDetailExtractor extends NotificationDetailExtractor<RuleViolationNotificationView> {
    @Autowired
    public RuleViolationNotificationDetailExtractor() {
        super(RuleViolationNotificationView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, RuleViolationNotificationView notificationView) {
        RuleViolationNotificationContent notificationContent = notificationView.getContent();
        return notificationContent.getPolicyInfos()
            .stream()
            .map(policyInfo -> createFlattenedContent(notificationContent, policyInfo))
            .map(content -> DetailedNotificationContent.policy(
                alertNotificationModel,
                content,
                notificationContent.getProjectName(),
                notificationContent.getProjectVersionName(),
                content.getPolicyInfo().getPolicyName()
            ))
            .collect(Collectors.toList());
    }

    private RuleViolationUniquePolicyNotificationContent createFlattenedContent(RuleViolationNotificationContent notificationContent, PolicyInfo policyInfo) {
        List<ComponentVersionStatus> validComponentStatuses = notificationContent.getComponentVersionStatuses()
            .stream()
            .filter(status -> status.getPolicies().contains(policyInfo.getPolicy()))
            .collect(Collectors.toList());
        return new RuleViolationUniquePolicyNotificationContent(
            notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            validComponentStatuses.size(),
            validComponentStatuses,
            policyInfo
        );
    }

}
