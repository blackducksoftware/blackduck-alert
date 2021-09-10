package com.synopsys.integration.alert.provider.blackduck.processor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public class PolicyOverrideUniquePolicyNotificationContentTest {
    private static final String PROJECT_NAME = "ProjectName";
    private static final String PROJECT_VERSION_NAME = "ProjectVersionName";
    private static final String PROJECT_VERSION_URL = "http://projectUrl";

    @Test
    public void getContentTest() {
        String componentName = "Component Name";
        String componentVersionName = "Component Version Name";
        String firstName = "firstname";
        String lastName = "lastname";
        PolicyInfo policyInfo = new PolicyInfo();
        String policy = "http://policyUrl";
        String bomComponentVersionPolicyStatus = "Some policy status";
        String bomComponent = "http://bomComponentUrl";

        PolicyOverrideUniquePolicyNotificationContent notificationContent = new PolicyOverrideUniquePolicyNotificationContent(
            PROJECT_NAME,
            PROJECT_VERSION_NAME,
            PROJECT_VERSION_URL,
            componentName,
            componentVersionName,
            firstName,
            lastName,
            policyInfo,
            policy,
            bomComponentVersionPolicyStatus,
            bomComponent
        );

        assertEquals(PROJECT_NAME, notificationContent.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, notificationContent.getProjectVersionName());
        assertEquals(PROJECT_VERSION_URL, notificationContent.getProjectVersionUrl());

        assertEquals(componentName, notificationContent.getComponentName());
        assertEquals(componentVersionName, notificationContent.getComponentVersionName());
        assertEquals(firstName, notificationContent.getFirstName());
        assertEquals(lastName, notificationContent.getLastName());
        assertEquals(policyInfo, notificationContent.getPolicyInfo());
        assertEquals(policy, notificationContent.getPolicy());
        assertEquals(bomComponentVersionPolicyStatus, notificationContent.getBomComponentVersionPolicyStatus());
        assertEquals(bomComponent, notificationContent.getBomComponent());
    }
}
