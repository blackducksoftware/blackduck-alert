package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.model.RuleViolationClearedUniquePolicyNotificationContent;
import com.blackduck.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;
import com.blackduck.integration.blackduck.api.manual.component.RuleViolationClearedNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;

@Component
public class RuleViolationClearedNotificationDetailExtractor extends NotificationDetailExtractor<RuleViolationClearedNotificationView> {
    @Autowired
    public RuleViolationClearedNotificationDetailExtractor() {
        super(RuleViolationClearedNotificationView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, RuleViolationClearedNotificationView notificationView) {
        RuleViolationClearedNotificationContent notificationContent = notificationView.getContent();
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

    private RuleViolationClearedUniquePolicyNotificationContent createFlattenedContent(RuleViolationClearedNotificationContent notificationContent, PolicyInfo policyInfo) {
        List<ComponentVersionStatus> validComponentStatuses = notificationContent.getComponentVersionStatuses()
            .stream()
            .filter(status -> status.getPolicies().contains(policyInfo.getPolicy()))
            .collect(Collectors.toList());
        return new RuleViolationClearedUniquePolicyNotificationContent(
            notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            validComponentStatuses.size(),
            validComponentStatuses,
            policyInfo
        );
    }

}
