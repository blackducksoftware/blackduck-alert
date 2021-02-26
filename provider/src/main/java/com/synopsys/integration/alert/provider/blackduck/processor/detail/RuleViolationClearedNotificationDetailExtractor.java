/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationClearedUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;

@Component
public class RuleViolationClearedNotificationDetailExtractor extends NotificationDetailExtractor<RuleViolationClearedNotificationContent, RuleViolationClearedNotificationView> {
    @Autowired
    public RuleViolationClearedNotificationDetailExtractor(Gson gson) {
        super(NotificationType.RULE_VIOLATION_CLEARED, RuleViolationClearedNotificationView.class, gson);
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, RuleViolationClearedNotificationContent notificationContent) {
        return notificationContent.getPolicyInfos()
                   .stream()
                   .map(policyInfo -> createFlattenedContent(notificationContent, policyInfo))
                   .map(content -> DetailedNotificationContent.policy(alertNotificationModel, content, notificationContent.getProjectName(), content.getPolicyInfo().getPolicyName()))
                   .collect(Collectors.toList());
    }

    private RuleViolationClearedUniquePolicyNotificationContent createFlattenedContent(RuleViolationClearedNotificationContent notificationContent, PolicyInfo policyInfo) {
        return new RuleViolationClearedUniquePolicyNotificationContent(
            notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            notificationContent.getComponentVersionsCleared(),
            notificationContent.getComponentVersionStatuses(),
            policyInfo
        );
    }

}
