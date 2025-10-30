/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.detail.ProjectVersionNotificationDetailExtractor;
import com.blackduck.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.enumeration.OperationType;
import com.blackduck.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NotificationDetailExtractionDelegatorTest {
    private static final Random RANDOM = new Random();
    private static final Gson GSON = new GsonBuilder().create();
    private static final BlackDuckResponseResolver RESPONSE_RESOLVER = new BlackDuckResponseResolver(GSON);
    private static final ProjectVersionNotificationView PVN_VIEW = createProjectVersionNotificationView();
    private static final AlertNotificationModel ALERT_NOTIFICATION_MODEL = createAlertNotification();

    @Test
    public void wrapNotificationTest() {
        ProjectVersionNotificationDetailExtractor projectVersionNDE = new ProjectVersionNotificationDetailExtractor();
        NotificationDetailExtractionDelegator delegator = new NotificationDetailExtractionDelegator(RESPONSE_RESOLVER, List.of(projectVersionNDE));

        List<DetailedNotificationContent> detailedContent = delegator.wrapNotification(ALERT_NOTIFICATION_MODEL);
        assertEquals(1, detailedContent.size());

        ProjectVersionNotificationContent content = PVN_VIEW.getContent();
        DetailedNotificationContent detailedPVN = detailedContent.get(0);
        assertEquals(content.getProjectName(), detailedPVN.getProjectName().orElse(null));
    }

    @Test
    public void wrapNotificationNoExtractorsTest() {
        NotificationDetailExtractionDelegator delegator = new NotificationDetailExtractionDelegator(RESPONSE_RESOLVER, List.of());
        List<DetailedNotificationContent> detailedContent = delegator.wrapNotification(ALERT_NOTIFICATION_MODEL);
        assertTrue(detailedContent.isEmpty(), "Expected no detailed content because there were no valid extractors");
    }

    private static AlertNotificationModel createAlertNotification() {
        String pvnJson = GSON.toJson(PVN_VIEW);
        return new AlertNotificationModel(
            RANDOM.nextLong(),
            RANDOM.nextLong(),
            "provider_blackduck",
            "provider-config",
            PVN_VIEW.getType().name(),
            pvnJson,
            OffsetDateTime.now(),
            OffsetDateTime.now().minusHours(1L),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }

    private static ProjectVersionNotificationView createProjectVersionNotificationView() {
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();
        projectVersionNotificationContent.setProject("https://my-project");
        projectVersionNotificationContent.setProjectName("my project!");
        projectVersionNotificationContent.setProjectVersion("https://project-version");
        projectVersionNotificationContent.setProjectVersionName("a name for a version 02468");
        projectVersionNotificationContent.setOperationType(OperationType.CREATE);
        ProjectVersionNotificationView projectVersionNotificationView = new ProjectVersionNotificationView();
        projectVersionNotificationView.setType(NotificationType.PROJECT_VERSION);
        projectVersionNotificationView.setContent(projectVersionNotificationContent);
        return projectVersionNotificationView;
    }

}
