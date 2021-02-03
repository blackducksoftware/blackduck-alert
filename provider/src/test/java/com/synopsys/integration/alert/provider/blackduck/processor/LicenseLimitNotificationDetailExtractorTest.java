package com.synopsys.integration.alert.provider.blackduck.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.processor.detail.LicenseLimitNotificationDetailExtractor;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class LicenseLimitNotificationDetailExtractorTest {
    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentTest() {
        LicenseLimitNotificationDetailExtractor extractor = new LicenseLimitNotificationDetailExtractor(gson);
        AlertNotificationModel notification = new AlertNotificationModel(0L, 0L, "BlackDuck", "Config 1", NotificationType.LICENSE_LIMIT.name(), "{}", null, null, false);

        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification);
        assertEquals(1, detailedNotificationContents.size());

        DetailedNotificationContent detailedNotificationContent = detailedNotificationContents.get(0);
        assertTrue(detailedNotificationContent.getProjectName().isEmpty(), "Expected no project name to be present");
        assertTrue(detailedNotificationContent.getPolicyName().isEmpty(), "Expected no policy name to be present");
        assertEquals(0, detailedNotificationContent.getVulnerabilitySeverities().size());
    }

}
