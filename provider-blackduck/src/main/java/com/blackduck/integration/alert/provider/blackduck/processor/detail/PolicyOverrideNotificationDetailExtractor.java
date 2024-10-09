package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.model.PolicyOverrideUniquePolicyNotificationContent;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;
import com.blackduck.integration.blackduck.api.manual.component.PolicyOverrideNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;

@Component
public class PolicyOverrideNotificationDetailExtractor extends NotificationDetailExtractor<PolicyOverrideNotificationView> {
    @Autowired
    public PolicyOverrideNotificationDetailExtractor() {
        super(PolicyOverrideNotificationView.class);
    }

    @Override
    public List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, PolicyOverrideNotificationView notificationView) {
        PolicyOverrideNotificationContent notificationContent = notificationView.getContent();
        return notificationContent.getPolicyInfos()
            .stream()
            .map(policyInfo -> createFlattenedContent(notificationContent, policyInfo))
            .map(content -> DetailedNotificationContent.policy(alertNotificationModel, content, notificationContent.getProjectName(), notificationContent.getProjectVersionName(), content.getPolicyInfo().getPolicyName()))
            .collect(Collectors.toList());
    }

    private PolicyOverrideUniquePolicyNotificationContent createFlattenedContent(PolicyOverrideNotificationContent notificationContent, PolicyInfo policyInfo) {
        return new PolicyOverrideUniquePolicyNotificationContent(
            notificationContent.getProjectName(),
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            notificationContent.getComponentName(),
            notificationContent.getComponentVersionName(),
            notificationContent.getFirstName(),
            notificationContent.getLastName(),
            policyInfo,
            policyInfo.getPolicy(),
            notificationContent.getBomComponentVersionPolicyStatus(),
            notificationContent.getBomComponent()
        );
    }

}
