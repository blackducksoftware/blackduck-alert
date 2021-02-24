package com.synopsys.integration.alert.processor.api.deatil;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractor;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationUserView;

public class NotificationDetailExtractorTest {
    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentExpectingNotificationContentTest() {
        TestExtractor testExtractor = new TestExtractor(gson);

        VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();

        AlertNotificationModel contentModel = createNotificationModel(content);
        try {
            testExtractor.extractDetailedContent(contentModel);
            fail("Expected an exception to be thrown");
        } catch (RuntimeException e) {
            // Pass
        }
    }

    @Test
    public void extractDetailedContentExpectingNotificationViewTest() {
        TestExtractor testExtractor = new TestExtractor(gson);

        VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        VulnerabilityNotificationUserView view = new VulnerabilityNotificationUserView();
        view.setContent(content);

        AlertNotificationModel viewModel = createNotificationModel(view);
        try {
            testExtractor.extractDetailedContent(viewModel);
        } catch (RuntimeException e) {
            fail("Expected no exception to be thrown");
        }
    }

    private AlertNotificationModel createNotificationModel(Object content) {
        String jsonContent = gson.toJson(content);
        return new AlertNotificationModel(
            0L,
            0L,
            "provider",
            "providerConfig",
            NotificationType.VULNERABILITY.name(),
            jsonContent,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false
        );
    }

    private static class TestExtractor extends NotificationDetailExtractor<VulnerabilityNotificationContent> {
        public TestExtractor(Gson gson) {
            super(NotificationType.VULNERABILITY, VulnerabilityNotificationContent.class, gson);
        }

        @Override
        protected List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, VulnerabilityNotificationContent notificationContent) {
            return List.of();
        }

    }

}
