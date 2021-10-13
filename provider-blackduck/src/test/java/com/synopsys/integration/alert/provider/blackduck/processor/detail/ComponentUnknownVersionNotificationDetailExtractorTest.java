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
import com.synopsys.integration.blackduck.api.manual.component.ComponentUnknownVersionContent;
import com.synopsys.integration.blackduck.api.manual.view.ComponentUnknownVersionView;

public class ComponentUnknownVersionNotificationDetailExtractorTest {
    public static final String NOTIFICATION_JSON_PATH = "json/componentUnknownVersionNotification.json";

    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentTest() throws IOException {
        String jsonContent = TestResourceUtils.readFileToString(NOTIFICATION_JSON_PATH);
        ComponentUnknownVersionView notificationView = gson.fromJson(jsonContent, ComponentUnknownVersionView.class);
        ComponentUnknownVersionContent notificationContent = notificationView.getContent();

        AlertNotificationModel notification = new AlertNotificationModel(0L, 0L, "BlackDuck", "Config 1", null, null, null, null, false);

        ComponentUnknownVersionNotificationDetailExtractor extractor = new ComponentUnknownVersionNotificationDetailExtractor();
        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);
        assertEquals(1, detailedNotificationContents.size());

        for (DetailedNotificationContent detailedContent : detailedNotificationContents) {
            Optional<String> optionalProjectName = detailedContent.getProjectName();
            assertTrue(optionalProjectName.isPresent(), "Expect project name to be present");
            assertEquals(notificationContent.getProjectName(), optionalProjectName.get());
            assertTrue(detailedContent.getPolicyName().isEmpty(), "Expected no policy name to be present");
            assertEquals(0, detailedContent.getVulnerabilitySeverities().size());
        }
    }
}
