package com.blackducksoftware.integration.hub.alert.processor.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;

public class PolicyViolationRuleTest {

    @Test
    public void testIsApplicableTrue() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationRule rule = new PolicyViolationRule(globalProperties);
        final RuleViolationNotificationContent content = createContent();
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);
        final NotificationContentDetail detail = NotificationGeneratorUtils.createNotificationDetailList(view, content).get(0);
        assertTrue(rule.isApplicable(detail));

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
        final RuleViolationNotificationContent content = createContent();

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);
        final NotificationContentDetail detail = NotificationGeneratorUtils.createNotificationDetailList(view, content).get(0);
        assertFalse(rule.isApplicable(detail));
    }

    @Test
    public void testApply() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationRule rule = new PolicyViolationRule(globalProperties);
        final Map<String, NotificationProcessingModel> modelMap = new HashMap<>();

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);

        final RuleViolationNotificationContent content = createContent();

        final List<NotificationContentDetail> detailList = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        notificationResults.getResults().forEach(notificationViewResult -> {
            rule.apply(modelMap, notificationViewResult, notificationResults.getHubBucket());
        });

        assertEquals(1, modelMap.size());
        final NotificationContentDetail contentDetail = notificationResults.getResults().get(0);
        final String key = contentDetail.getContentDetailKey();
        final NotificationProcessingModel model = modelMap.get(key);

        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION, model.getNotificationType());
        assertEquals(contentDetail, model.getContentDetail());
    }

    private RuleViolationNotificationContent createContent() {
        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.2.3";
        content.projectVersion = "policy url";
        content.componentVersionsInViolation = 1;

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
        componentVersionStatus.bomComponentVersionPolicyStatus = "IN_VIOLATION";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);

        return content;
    }

}
