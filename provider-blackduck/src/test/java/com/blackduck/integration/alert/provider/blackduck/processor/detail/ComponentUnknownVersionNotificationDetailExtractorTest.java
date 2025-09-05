/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.model.ComponentUnknownVersionWithStatusNotificationContent;
import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.blackduck.api.manual.component.ComponentUnknownVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.ComponentUnknownVersionNotificationView;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

public class ComponentUnknownVersionNotificationDetailExtractorTest {
    public static final String NOTIFICATION_JSON_PATH = "json/componentUnknownVersionNotification.json";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Test
    public void extractDetailedContentTest() throws IOException {
        String jsonContent = TestResourceUtils.readFileToString(NOTIFICATION_JSON_PATH);
        ComponentUnknownVersionNotificationView notificationView = gson.fromJson(jsonContent, ComponentUnknownVersionNotificationView.class);
        ComponentUnknownVersionNotificationContent notificationContent = notificationView.getContent();

        AlertNotificationModel notification = new AlertNotificationModel(
            0L,
            0L,
            "BlackDuck",
            "Config 1",
            null,
            null,
            null,
            null,
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );

        ComponentUnknownVersionNotificationDetailExtractor extractor = new ComponentUnknownVersionNotificationDetailExtractor();
        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);
        assertEquals(1, detailedNotificationContents.size());

        for (DetailedNotificationContent detailedContent : detailedNotificationContents) {
            Optional<String> optionalProjectName = detailedContent.getProjectName();
            assertTrue(optionalProjectName.isPresent(), "Expect project name to be present");
            assertEquals(notificationContent.getProjectName(), optionalProjectName.get());
            assertTrue(detailedContent.getPolicyName().isEmpty(), "Expected no policy name to be present");
            assertEquals(0, detailedContent.getVulnerabilitySeverities().size());
            assertEquals(ComponentUnknownVersionWithStatusNotificationContent.class, detailedContent.getNotificationContentWrapper().getNotificationContentClass());
            ComponentUnknownVersionWithStatusNotificationContent content = (ComponentUnknownVersionWithStatusNotificationContent) detailedContent.getNotificationContentWrapper().getNotificationContent();
            assertEquals("project", content.getProjectName());
            assertEquals("1", content.getProjectVersionName());
            assertEquals("Apache Tomcat", content.getComponentName());

            assertEquals(1, content.getCriticalVulnerabilityCount());
            assertEquals(8, content.getHighVulnerabilityCount());
            assertEquals(58, content.getMediumVulnerabilityCount());
            assertEquals(29, content.getLowVulnerabilityCount());

            assertEquals("0.0.1", content.getCriticalVulnerabilityVersionName());
            assertEquals("6.0.0", content.getHighVulnerabilityVersionName());
            assertEquals("6.0.0", content.getMediumVulnerabilityVersionName());
            assertEquals("TOMCAT_9_0_0_M7", content.getLowVulnerabilityVersionName());

            assertTrue(StringUtils.isNotBlank(content.getBomComponent()));
            assertTrue(StringUtils.isNotBlank(content.getComponent()));
            assertTrue(StringUtils.isNotBlank(content.getCriticalVulnerabilityVersion()));
            assertTrue(StringUtils.isNotBlank(content.getHighVulnerabilityVersion()));
            assertTrue(StringUtils.isNotBlank(content.getMediumVulnerabilityVersion()));
            assertTrue(StringUtils.isNotBlank(content.getLowVulnerabilityVersion()));
        }
    }
}
