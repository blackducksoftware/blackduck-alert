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
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;

public class PolicyViolationRuleClearedTest {

    @Test
    public void testIsApplicableTrue() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationClearedRule rule = new PolicyViolationClearedRule(globalProperties);
        final RuleViolationClearedNotificationContent content = createContent();
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION_CLEARED);
        final NotificationViewResult notificationViewResult = NotificationGeneratorUtils.createNotificationViewResult(view, content);
        assertTrue(rule.isApplicable(notificationViewResult));

    }

    @Test
    public void testIsApplicableFalse() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationClearedRule rule = new PolicyViolationClearedRule(globalProperties);

        final RuleViolationClearedNotificationContent content = createContent();

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);
        final NotificationViewResult notificationViewResult = NotificationGeneratorUtils.createNotificationViewResult(view, content);
        assertFalse(rule.isApplicable(notificationViewResult));
    }

    @Test
    public void testApply() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationClearedRule rule = new PolicyViolationClearedRule(globalProperties);
        final Map<String, NotificationProcessingModel> modelMap = new HashMap<>();

        final List<CommonNotificationState> notificationContentItems = new ArrayList<>();

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION_CLEARED);

        final RuleViolationClearedNotificationContent content = createContent();
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

        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, model.getNotificationType());
        assertEquals(notificationContentItem, model.getCommonNotificationState());
        assertEquals(contentDetail, model.getContentDetail());
    }

    public RuleViolationClearedNotificationContent createContent() {
        final RuleViolationClearedNotificationContent content = new RuleViolationClearedNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.2.3";
        content.projectVersion = "policy url";
        content.componentVersionsCleared = 1;

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolation";
        policyInfo.policy = "policyUrl";
        content.policyInfos = Arrays.asList(policyInfo);

        final String componentName = "notification test component";
        final String componentVersionName = "1.2.3";
        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.componentName = componentName;
        componentVersionStatus.componentVersionName = componentVersionName;
        componentVersionStatus.component = "component url";
        componentVersionStatus.componentVersion = "component version url";
        componentVersionStatus.componentIssueLink = "issuesLink";
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "VIOLATION_CLEARED";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);

        return content;
    }

}
