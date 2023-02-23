package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;

public class RuleViolationNotificationDetailExtractorTest {
    public static final String NOTIFICATION_JSON_PATH = "json/ruleViolationNotification.json";
    public static final String DUPLICATE_NOTIFICATION_JSON_PATH = "json/ruleViolationDuplicateNotification.json";

    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentTest() throws IOException {
        ContentAndDetailedContent contentAndDetailedContent = createAndProduceNotificationDetails(NOTIFICATION_JSON_PATH);
        List<DetailedNotificationContent> detailedNotificationContents = contentAndDetailedContent.detailedNotificationContents;
        assertEquals(2, detailedNotificationContents.size());

        for (DetailedNotificationContent detailedContent : detailedNotificationContents) {
            Optional<String> optionalProjectName = detailedContent.getProjectName();
            assertTrue(optionalProjectName.isPresent(), "Expect project name to be present");
            assertEquals(contentAndDetailedContent.ruleViolationNotificationContent.getProjectName(), optionalProjectName.get());

            Optional<String> optionalPolicyName = detailedContent.getPolicyName();
            assertTrue(optionalPolicyName.isPresent(), "Expected policy name to be present");

            boolean containsPolicy = contentAndDetailedContent.ruleViolationNotificationContent.getPolicyInfos()
                .stream()
                .map(PolicyInfo::getPolicyName)
                .anyMatch(policyName -> policyName.equals(optionalPolicyName.get()));
            assertTrue(containsPolicy, "Expected policy name to be present in original notification");

            assertEquals(0, detailedContent.getVulnerabilitySeverities().size());
        }
    }

    @Test
    public void extractDetailedContentWithDuplicatesTest() throws IOException {
        ContentAndDetailedContent contentAndDetailedContent = createAndProduceNotificationDetails(DUPLICATE_NOTIFICATION_JSON_PATH);
        List<DetailedNotificationContent> detailedNotificationContents = contentAndDetailedContent.detailedNotificationContents;

        assertEquals(4, detailedNotificationContents.size());
        for (DetailedNotificationContent notificationContent : detailedNotificationContents) {
            RuleViolationUniquePolicyNotificationContent policyContent = RuleViolationUniquePolicyNotificationContent.class.cast(notificationContent.getNotificationContentWrapper()
                .getNotificationContent());

            Optional<String> policyNameOptional = notificationContent.getPolicyName();
            assertTrue(policyNameOptional.isPresent());

            String policyName = policyNameOptional.get();
            if ("BM Not Reviewed".equals(policyName)) {
                assertEquals(94, policyContent.getComponentVersionStatuses().size());
            } else if ("Reciprocal".equals(policyName)) {
                assertEquals(3, policyContent.getComponentVersionStatuses().size());
            } else if ("GitHub Action Policy".equals(policyName)) {
                assertEquals(1, policyContent.getComponentVersionStatuses().size());
            } else if ("BM_Testing-SolutionAvailable-False".equals(policyName)) {
                assertEquals(4, policyContent.getComponentVersionStatuses().size());
            } else {
                fail("Should have had a policy name that matches");
            }
        }
    }

    private ContentAndDetailedContent createAndProduceNotificationDetails(String jsonPath) throws IOException {
        String jsonContent = TestResourceUtils.readFileToString(jsonPath);
        RuleViolationNotificationView notificationView = gson.fromJson(jsonContent, RuleViolationNotificationView.class);

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

        RuleViolationNotificationDetailExtractor extractor = new RuleViolationNotificationDetailExtractor();
        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);
        return new ContentAndDetailedContent(notificationView.getContent(), detailedNotificationContents);
    }

    private class ContentAndDetailedContent {
        RuleViolationNotificationContent ruleViolationNotificationContent;
        List<DetailedNotificationContent> detailedNotificationContents;

        public ContentAndDetailedContent(
            RuleViolationNotificationContent ruleViolationNotificationContent,
            List<DetailedNotificationContent> detailedNotificationContents
        ) {
            this.ruleViolationNotificationContent = ruleViolationNotificationContent;
            this.detailedNotificationContents = detailedNotificationContents;
        }
    }

}
