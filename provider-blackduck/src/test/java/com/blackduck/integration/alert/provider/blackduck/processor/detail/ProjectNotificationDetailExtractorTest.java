package com.blackduck.integration.alert.provider.blackduck.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.blackduck.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

public class ProjectNotificationDetailExtractorTest {
    public static final String NOTIFICATION_JSON_PATH = "json/projectNotification.json";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Test
    public void extractDetailedContentTest() throws IOException {
        String jsonContent = TestResourceUtils.readFileToString(NOTIFICATION_JSON_PATH);
        ProjectNotificationView projectNotificationView = gson.fromJson(jsonContent, ProjectNotificationView.class);
        ProjectNotificationContent projectNotificationContent = projectNotificationView.getContent();

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
            String.format("content-id-%s", UUID.randomUUID())
        );

        ProjectNotificationDetailExtractor extractor = new ProjectNotificationDetailExtractor();
        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, projectNotificationView);
        assertEquals(1, detailedNotificationContents.size());

        DetailedNotificationContent detailedNotificationContent = detailedNotificationContents.get(0);

        Optional<String> optionalProjectName = detailedNotificationContent.getProjectName();
        assertTrue(optionalProjectName.isPresent(), "Expect project name to be present");

        assertEquals(projectNotificationContent.getProjectName(), optionalProjectName.get());
        assertTrue(detailedNotificationContent.getPolicyName().isEmpty(), "Expected no policy name to be present");
        assertEquals(0, detailedNotificationContent.getVulnerabilitySeverities().size());
    }

}
