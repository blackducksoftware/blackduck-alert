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
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;

public class PolicyViolationRuleTest {

    @Test
    public void testIsApplicableTrue() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationRule rule = new PolicyViolationRule(globalProperties);

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);
        final CommonNotificationState commonNotificationState = NotificationGeneratorUtils.createCommonNotificationState(view, null);
        assertTrue(rule.isApplicable(commonNotificationState));

    }

    @Test
    public void testGetGlobalProperties() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationRule rule = new PolicyViolationRule(globalProperties);

        assertEquals(globalProperties, rule.getGlobalProperties());
    }

    @Test
    public void testIsApplicableFalse() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationRule rule = new PolicyViolationRule(globalProperties);

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);
        final CommonNotificationState commonNotificationState = NotificationGeneratorUtils.createCommonNotificationState(view, null);
        assertFalse(rule.isApplicable(commonNotificationState));
    }

    @Test
    public void testApply() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationRule rule = new PolicyViolationRule(globalProperties);
        final Map<String, NotificationProcessingModel> modelMap = new HashMap<>();

        final List<CommonNotificationState> notificationContentItems = new ArrayList<>();
        final String componentName = "notification test component";
        final String componentVersionName = "1.2.3";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);

        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.2.3";
        content.projectVersion = "policy url";
        content.componentVersionsInViolation = 1;

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolation";
        policyInfo.policy = "policyUrl";
        content.policyInfos = Arrays.asList(policyInfo);

        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.componentName = componentName;
        componentVersionStatus.componentVersionName = componentVersionName;
        componentVersionStatus.component = "component url";
        componentVersionStatus.componentVersion = "component version url";
        componentVersionStatus.componentIssueLink = "issuesLink";
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "IN_VIOLATION";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);

        final CommonNotificationState notificationContentItem = NotificationGeneratorUtils.createCommonNotificationState(view, content);
        notificationContentItems.add(notificationContentItem);

        final NotificationResults notificationResults = NotificationGeneratorUtils.createNotificationResults(notificationContentItems);

        notificationResults.getCommonNotificationStates().forEach(commonNotificationState -> {
            rule.apply(modelMap, commonNotificationState, notificationResults.getHubBucket());
        });

        assertEquals(1, modelMap.size());
        final NotificationContentDetail contentDetail = content.createNotificationContentDetails().get(0);
        final String key = contentDetail.getContentDetailKey();
        final NotificationProcessingModel model = modelMap.get(key);

        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION, model.getNotificationType());
        assertEquals(notificationContentItem, model.getCommonNotificationState());
        assertEquals(contentDetail, model.getContentDetail());
    }

}
