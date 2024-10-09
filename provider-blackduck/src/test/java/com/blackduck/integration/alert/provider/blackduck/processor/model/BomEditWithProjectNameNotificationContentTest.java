package com.blackduck.integration.alert.provider.blackduck.processor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.provider.blackduck.processor.model.BomEditWithProjectNameNotificationContent;
import com.blackduck.integration.blackduck.api.manual.component.BomEditNotificationContent;

public class BomEditWithProjectNameNotificationContentTest {
    private static final String PROJECT_NAME = "ProjectName";
    private static final String PROJECT_VERSION_NAME = "ProjectVersionName";
    private static final String PROJECT_VERSION_URL = "http://projectUrl";

    @Test
    public void getContentTest() {
        String bomComponent = "http://bomComponentLink";
        String componentName = "Component Name";
        String componentVersionName = "ComponentVersionName";

        BomEditNotificationContent bomEditNotificationContent = new BomEditNotificationContent();
        bomEditNotificationContent.setProjectVersion(PROJECT_VERSION_URL);
        bomEditNotificationContent.setBomComponent(bomComponent);
        bomEditNotificationContent.setComponentName(componentName);
        bomEditNotificationContent.setComponentVersionName(componentVersionName);

        BomEditWithProjectNameNotificationContent notificationContent = new BomEditWithProjectNameNotificationContent(bomEditNotificationContent, PROJECT_NAME, PROJECT_VERSION_NAME);

        assertEquals(PROJECT_NAME, notificationContent.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, notificationContent.getProjectVersionName());
        assertEquals(PROJECT_VERSION_URL, notificationContent.getProjectVersionUrl());

        assertEquals(bomComponent, notificationContent.getBomComponent());
        assertEquals(componentName, notificationContent.getComponentName());
        assertEquals(componentVersionName, notificationContent.getComponentVersionName());
    }
}
