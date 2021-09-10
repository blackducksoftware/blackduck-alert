package com.synopsys.integration.alert.provider.blackduck.processor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public class RuleViolationUniquePolicyNotificationContentTest {
    private static final String PROJECT_NAME = "ProjectName";
    private static final String PROJECT_VERSION_NAME = "ProjectVersionName";
    private static final String PROJECT_VERSION_URL = "http://projectUrl";

    @Test
    public void getContentsTest() {
        int componentVersionsInViolation = 1;
        ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        PolicyInfo policyInfo = new PolicyInfo();

        RuleViolationUniquePolicyNotificationContent notificationContent = new RuleViolationUniquePolicyNotificationContent(
            PROJECT_NAME,
            PROJECT_VERSION_NAME,
            PROJECT_VERSION_URL,
            componentVersionsInViolation,
            List.of(componentVersionStatus),
            policyInfo
        );

        assertEquals(PROJECT_NAME, notificationContent.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, notificationContent.getProjectVersionName());
        assertEquals(PROJECT_VERSION_URL, notificationContent.getProjectVersionUrl());

        assertEquals(componentVersionsInViolation, notificationContent.getComponentVersionsInViolation());
        assertTrue(notificationContent.getComponentVersionStatuses().contains(componentVersionStatus));
        assertEquals(policyInfo, notificationContent.getPolicyInfo());
    }
}
