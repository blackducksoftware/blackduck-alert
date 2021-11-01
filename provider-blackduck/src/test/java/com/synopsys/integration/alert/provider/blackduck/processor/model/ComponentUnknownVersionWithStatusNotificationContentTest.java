package com.synopsys.integration.alert.provider.blackduck.processor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.manual.enumeration.ComponentUnknownVersionStatus;

public class ComponentUnknownVersionWithStatusNotificationContentTest {
    private static final String PROJECT_NAME = "ProjectName";
    private static final String PROJECT_VERSION_NAME = "ProjectVersionName";
    private static final String PROJECT_VERSION_URL = "http://projectUrl";

    @Test
    public void getContentTest() {
        String componentName = "Component Name";
        String componentUrl = "http://componentUrl";
        String bomComponent = "http://bomComponentUrl";
        int criticalCount = 1;
        String criticalVersionName = "Critical Version Name";
        String criticalVersionUrl = "http://criticalUrl";
        int highCount = 2;
        String highVersionName = "High Version Name";
        String hightVersionUrl = "http://highUrl";
        int mediumCount = 3;
        String mediumVersionName = "Medium Version Name";
        String mediumVersionUrl = "http://mediumUrl";
        int lowCount = 4;
        String lowVersionName = "Low Version Name";
        String lowVersionUrl = "http://lowUrl";

        ComponentUnknownVersionWithStatusNotificationContent model = new ComponentUnknownVersionWithStatusNotificationContent(PROJECT_NAME,
            PROJECT_VERSION_NAME,
            PROJECT_VERSION_URL,
            componentName,
            bomComponent,
            componentUrl,
            criticalCount,
            criticalVersionUrl,
            criticalVersionName,
            highCount,
            hightVersionUrl,
            highVersionName,
            mediumCount,
            mediumVersionUrl,
            mediumVersionName,
            lowCount,
            lowVersionUrl,
            lowVersionName,
            ComponentUnknownVersionStatus.FOUND);

        assertEquals(PROJECT_NAME, model.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, model.getProjectVersionName());
        assertEquals(PROJECT_VERSION_URL, model.getProjectVersionUrl());
        assertEquals(componentName, model.getComponentName());
        assertEquals(componentUrl, model.getComponent());
        assertEquals(bomComponent, model.getBomComponent());
        assertEquals(criticalCount, model.getCriticalVulnerabilityCount());
        assertEquals(criticalVersionName, model.getCriticalVulnerabilityVersionName());
        assertEquals(criticalVersionUrl, model.getCriticalVulnerabilityVersion());
        assertEquals(highCount, model.getHighVulnerabilityCount());
        assertEquals(highVersionName, model.getHighVulnerabilityVersionName());
        assertEquals(hightVersionUrl, model.getHighVulnerabilityVersion());
        assertEquals(mediumCount, model.getMediumVulnerabilityCount());
        assertEquals(mediumVersionName, model.getMediumVulnerabilityVersionName());
        assertEquals(mediumVersionUrl, model.getMediumVulnerabilityVersion());
        assertEquals(lowCount, model.getLowVulnerabilityCount());
        assertEquals(lowVersionName, model.getLowVulnerabilityVersionName());
        assertEquals(lowVersionUrl, model.getLowVulnerabilityVersion());
        assertEquals(ComponentUnknownVersionStatus.FOUND, model.getStatus());
    }

}
