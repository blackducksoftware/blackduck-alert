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

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.LicenseLimitNotificationView;

public class LicenseLimitNotificationDetailExtractorTest {
    @Test
    public void extractDetailedContentTest() {
        LicenseLimitNotificationDetailExtractor extractor = new LicenseLimitNotificationDetailExtractor();

        LicenseLimitNotificationView notificationView = new LicenseLimitNotificationView();
        LicenseLimitNotificationContent notificationContent = new LicenseLimitNotificationContent();
        notificationView.setContent(notificationContent);

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

        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);
        assertEquals(1, detailedNotificationContents.size());

        DetailedNotificationContent detailedNotificationContent = detailedNotificationContents.get(0);
        assertTrue(detailedNotificationContent.getProjectName().isEmpty(), "Expected no project name to be present");
        assertTrue(detailedNotificationContent.getPolicyName().isEmpty(), "Expected no policy name to be present");
        assertEquals(0, detailedNotificationContent.getVulnerabilitySeverities().size());
    }

}
