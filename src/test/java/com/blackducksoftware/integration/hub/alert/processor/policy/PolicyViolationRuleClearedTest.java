package com.blackducksoftware.integration.hub.alert.processor.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;

public class PolicyViolationRuleClearedTest {

    @Test
    public void testIsApplicableTrue() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationClearedRule rule = new PolicyViolationClearedRule(globalProperties);
        final NotificationGeneratorUtils notificationUtils = new NotificationGeneratorUtils();

        final NotificationView view = notificationUtils.createNotificationView(NotificationType.RULE_VIOLATION_CLEARED);
        final CommonNotificationState commonNotificationState = notificationUtils.createCommonNotificationState(view, null);
        assertTrue(rule.isApplicable(commonNotificationState));

    }

    @Test
    public void testIsApplicableFalse() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PolicyViolationClearedRule rule = new PolicyViolationClearedRule(globalProperties);
        final NotificationGeneratorUtils notificationUtils = new NotificationGeneratorUtils();

        final NotificationView view = notificationUtils.createNotificationView(NotificationType.VULNERABILITY);
        final CommonNotificationState commonNotificationState = notificationUtils.createCommonNotificationState(view, null);
        assertFalse(rule.isApplicable(commonNotificationState));
    }

}
