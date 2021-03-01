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
import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;

@Component
public class RuleViolationNotificationDetailExtractor extends NotificationDetailExtractor<RuleViolationNotificationContent, RuleViolationNotificationView> {
    @Autowired
    public RuleViolationNotificationDetailExtractor(Gson gson) {
        super(NotificationType.RULE_VIOLATION, RuleViolationNotificationView.class, gson);
    }

    @Override
    protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, RuleViolationNotificationContent notificationContent) {
        return notificationContent.getPolicyInfos()
                   .stream()
                   .map(policyInfo -> createFlattenedContent(notificationContent, policyInfo))
                   .map(content -> DetailedNotificationContent.policy(alertNotificationModel, content, notificationContent.getProjectName(), content.getPolicyInfo().getPolicyName()))
                   .collect(Collectors.toList());
    }

    private RuleViolationUniquePolicyNotificationContent createFlattenedContent(RuleViolationNotificationContent notificationContent, PolicyInfo policyInfo) {
        return new RuleViolationUniquePolicyNotificationContent(
            notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            notificationContent.getComponentVersionsInViolation(),
            notificationContent.getComponentVersionStatuses(),
            policyInfo
        );
    }

}
