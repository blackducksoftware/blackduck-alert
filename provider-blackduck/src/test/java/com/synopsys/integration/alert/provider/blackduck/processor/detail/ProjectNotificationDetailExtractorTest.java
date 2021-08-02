package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.blackduck.api.manual.component.ProjectNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;

public class ProjectNotificationDetailExtractorTest {
    public static final String NOTIFICATION_JSON_PATH = "json/projectNotification.json";

    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentTest() throws IOException {
        String jsonContent = TestResourceUtils.readFileToString(NOTIFICATION_JSON_PATH);
        ProjectNotificationView projectNotificationView = gson.fromJson(jsonContent, ProjectNotificationView.class);
        ProjectNotificationContent projectNotificationContent = projectNotificationView.getContent();

        AlertNotificationModel notification = new AlertNotificationModel(0L, 0L, "BlackDuck", "Config 1", null, null, null, null, false);

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
