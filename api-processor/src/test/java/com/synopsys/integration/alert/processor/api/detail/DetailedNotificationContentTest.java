package com.synopsys.integration.alert.processor.api.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;

public class DetailedNotificationContentTest {
    private static final String EXPECTED_NO_POLICY = "Expected no policy name";
    private static final AlertNotificationModel ALERT_NOTIFICATION_MODEL = new AlertNotificationModel(25L, 123L, null, null, null, null, null, null, false);

    @Test
    public void vulnerabilityTest() {
        String projectName = "vuln project";
        String projectVersionName = "version";
        List<String> severities = List.of("S1", "S2");
        VulnerabilityNotificationContent vulnerabilityNotificationContent = new VulnerabilityNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.vulnerability(ALERT_NOTIFICATION_MODEL, vulnerabilityNotificationContent, projectName, projectVersionName, severities);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), vulnerabilityNotificationContent.getClass(), severities);
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
    }

    @Test
    public void policyTest() {
        String projectName = "policy project";
        String projectVersionName = "version";
        String policyName = "policy name 01";
        RuleViolationNotificationContent ruleViolationNotificationContent = new RuleViolationNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.policy(ALERT_NOTIFICATION_MODEL, ruleViolationNotificationContent, projectName, projectVersionName, policyName);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), ruleViolationNotificationContent.getClass(), List.of());
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertEquals(policyName, detailedContent.getPolicyName().orElse(null));
    }

    @Test
    public void projectTest() {
        String projectName = "project with version";
        String projectVersionName = "version";
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.project(ALERT_NOTIFICATION_MODEL, projectVersionNotificationContent, projectName, projectVersionName);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), projectVersionNotificationContent.getClass(), List.of());
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
        assertEquals(projectVersionName, detailedContent.getProjectVersionName().orElse(null));
    }

    @Test
    public void projectlessTest() {
        LicenseLimitNotificationContent licenseLimitNotificationContent = new LicenseLimitNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.projectless(ALERT_NOTIFICATION_MODEL, licenseLimitNotificationContent);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), licenseLimitNotificationContent.getClass(), List.of());
        assertTrue(detailedContent.getProjectName().isEmpty(), "Expected no project name");
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
    }

    @Test
    public void versionlessTest() {
        String projectName = "project with version";
        String projectVersionName = "version";
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();
        DetailedNotificationContent detailedContent = DetailedNotificationContent.versionLess(ALERT_NOTIFICATION_MODEL, projectVersionNotificationContent, projectName);
        assertContent(detailedContent, ALERT_NOTIFICATION_MODEL.getProviderConfigId(), projectVersionNotificationContent.getClass(), List.of());
        assertEquals(projectName, detailedContent.getProjectName().orElse(null));
        assertTrue(detailedContent.getPolicyName().isEmpty(), EXPECTED_NO_POLICY);
        assertNull(detailedContent.getProjectVersionName().orElse(null));
    }

    private static void assertContent(DetailedNotificationContent content, Long providerConfigId, Class<? extends NotificationContentComponent> notificationClass, List<String> vulnerabilitySeverities) {
        assertEquals(providerConfigId, content.getProviderConfigId());
        assertEquals(vulnerabilitySeverities, content.getVulnerabilitySeverities());
        assertEquals(notificationClass, content.getNotificationContentWrapper().getNotificationContentClass());
    }

}
