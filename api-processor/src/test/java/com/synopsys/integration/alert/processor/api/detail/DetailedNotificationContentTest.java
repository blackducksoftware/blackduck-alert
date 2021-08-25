package com.synopsys.integration.alert.processor.api.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;

class DetailedNotificationContentTest {
    private static final String EXPECTED_NO_POLICY = "Expected no policy name";
    private static final AlertNotificationModel ALERT_NOTIFICATION_MODEL = new AlertNotificationModel(25L, 123L, null, null, null, null, null, null, false);

    @Test
    void vulnerabilityTest() {
        String projectName = "vuln project";
        List<String> severities = List.of("S1", "S2");
        VulnerabilityNotificationContent vulnerabilityNotificationContent = new VulnerabilityNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.vulnerability(ALERT_NOTIFICATION_MODEL, vulnerabilityNotificationContent, projectName, severities);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), vulnerabilityNotificationContent.getClass(), severities);
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
    }

    @Test
    void policyTest() {
        String projectName = "policy project";
        String policyName = "policy name 01";
        RuleViolationNotificationContent ruleViolationNotificationContent = new RuleViolationNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.policy(ALERT_NOTIFICATION_MODEL, ruleViolationNotificationContent, projectName, policyName);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), ruleViolationNotificationContent.getClass(), List.of());
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertEquals(policyName, detailedContent.getPolicyName().orElse(null));
    }

    @Test
    void projectTest() {
        String projectName = "project with version";
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.project(ALERT_NOTIFICATION_MODEL, projectVersionNotificationContent, projectName);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), projectVersionNotificationContent.getClass(), List.of());
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
    }

    @Test
    void projectlessTest() {
        LicenseLimitNotificationContent licenseLimitNotificationContent = new LicenseLimitNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.projectless(ALERT_NOTIFICATION_MODEL, licenseLimitNotificationContent);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), licenseLimitNotificationContent.getClass(), List.of());
        assertTrue(detailedContent.getProjectName().isEmpty(), "Expected no project name");
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
    }

    private static void assertContent(DetailedNotificationContent content, Long providerConfigId, Class<? extends NotificationContentComponent> notificationClass, List<String> vulnerabilitySeverities) {
        assertEquals(providerConfigId, content.getProviderConfigId());
        assertEquals(vulnerabilitySeverities, content.getVulnerabilitySeverities());
        assertEquals(notificationClass, content.getNotificationContentWrapper().getNotificationContentClass());
    }

}
