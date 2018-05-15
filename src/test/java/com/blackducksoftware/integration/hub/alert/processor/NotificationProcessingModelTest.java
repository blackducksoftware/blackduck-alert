package com.blackducksoftware.integration.hub.alert.processor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;

public class NotificationProcessingModelTest {

    @Test
    public void testNotificationModel() {
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
        componentVersionStatus.componentName = "notification test component";
        componentVersionStatus.componentVersionName = "1.2.3";
        componentVersionStatus.component = "component url";
        componentVersionStatus.componentVersion = "component version url";
        componentVersionStatus.componentIssueLink = "issuesLink";
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "IN_VIOLATION";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        final NotificationContentDetail detail = content.createNotificationContentDetails().get(0);

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);
        final CommonNotificationState commonNotificationState = NotificationGeneratorUtils.createCommonNotificationState(view, content);

        final NotificationProcessingModel model = new NotificationProcessingModel(detail, commonNotificationState, content, NotificationCategoryEnum.POLICY_VIOLATION);
        assertEquals(commonNotificationState, model.getCommonNotificationState());
        assertEquals(detail, model.getContentDetail());
        assertEquals(content, model.getContent());
        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION, model.getNotificationType());
    }
}
