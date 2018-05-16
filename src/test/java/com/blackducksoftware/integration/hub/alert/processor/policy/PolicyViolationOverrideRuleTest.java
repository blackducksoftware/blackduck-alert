package com.blackducksoftware.integration.hub.alert.processor.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.alert.processor.NotificationProcessingModel;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.NotificationViewResult;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;

public class PolicyViolationOverrideRuleTest {

    @Test
    public void testIsApplicableTrue() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationOverrideRule rule = new PolicyViolationOverrideRule(globalProperties);

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolation";
        policyInfo.policy = "policyUrl";
        final String componentName = "notification test component";
        final String componentVersionName = "1.2.3";
        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.2.3";
        content.projectVersion = "policy url";
        content.componentName = componentName;
        content.componentVersionName = componentVersionName;
        content.componentVersion = "componentVersionUrl";
        content.policyInfos = Arrays.asList(policyInfo);
        content.policies = Arrays.asList(policyInfo.policy);
        content.bomComponentVersionPolicyStatus = "POLICY_OVERRIDE";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.POLICY_OVERRIDE);
        final NotificationViewResult notificationViewResult = NotificationGeneratorUtils.createNotificationViewResult(view, content);
        assertTrue(rule.isApplicable(notificationViewResult));

    }

    @Test
    public void testIsApplicableFalse() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationOverrideRule rule = new PolicyViolationOverrideRule(globalProperties);
        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolation";
        policyInfo.policy = "policyUrl";
        final String componentName = "notification test component";
        final String componentVersionName = "1.2.3";
        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.2.3";
        content.projectVersion = "policy url";
        content.componentName = componentName;
        content.componentVersionName = componentVersionName;
        content.componentVersion = "componentVersionUrl";
        content.policyInfos = Arrays.asList(policyInfo);
        content.policies = Arrays.asList(policyInfo.policy);
        content.bomComponentVersionPolicyStatus = "POLICY_OVERRIDE";
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);
        final NotificationViewResult notificationViewResult = NotificationGeneratorUtils.createNotificationViewResult(view, content);
        assertFalse(rule.isApplicable(notificationViewResult));
    }

    @Test
    public void testApply() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationOverrideRule rule = new PolicyViolationOverrideRule(globalProperties);
        final Map<String, NotificationProcessingModel> modelMap = new HashMap<>();

        final List<CommonNotificationState> notificationContentItems = new ArrayList<>();

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.POLICY_OVERRIDE);
        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolation";
        policyInfo.policy = "policyUrl";
        final String componentName = "notification test component";
        final String componentVersionName = "1.2.3";
        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.2.3";
        content.projectVersion = "policy url";
        content.componentName = componentName;
        content.componentVersionName = componentVersionName;
        content.componentVersion = "componentVersionUrl";
        content.policyInfos = Arrays.asList(policyInfo);
        content.policies = Arrays.asList(policyInfo.policy);
        content.bomComponentVersionPolicyStatus = "POLICY_OVERRIDE";

        final CommonNotificationState notificationContentItem = NotificationGeneratorUtils.createCommonNotificationState(view, content);
        notificationContentItems.add(notificationContentItem);

        final NotificationResults notificationResults = NotificationGeneratorUtils.createNotificationResults(notificationContentItems);
        notificationResults.getNotificationViewResults().getResultList().forEach(notificationViewResult -> {
            rule.apply(modelMap, notificationViewResult, notificationResults.getHubBucket());
        });

        assertEquals(1, modelMap.size());
        final NotificationContentDetail contentDetail = notificationResults.getNotificationViewResults().getResultList().get(0).getNotificationContentDetails().get(0);
        final String key = contentDetail.getContentDetailKey();
        final NotificationProcessingModel model = modelMap.get(key);

        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, model.getNotificationType());
        assertEquals(notificationContentItem, model.getCommonNotificationState());
        assertEquals(contentDetail, model.getContentDetail());
    }

}
