package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;

public class PolicyOverrideNotificationDetailExtractorTest {
    public static final String POLICY_OVERRIDE_JSON_PATH = "json/policyOverrideNotification.json";

    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentTest() throws IOException {
        String notificationString = TestResourceUtils.readFileToString(POLICY_OVERRIDE_JSON_PATH);
        PolicyOverrideNotificationView notificationView = gson.fromJson(notificationString, PolicyOverrideNotificationView.class);
        PolicyOverrideNotificationContent notificationContent = notificationView.getContent();

        AlertNotificationModel notification = new AlertNotificationModel(0L, 0L, "BlackDuck", "Config 1", null, null, null, null, false);

        PolicyOverrideNotificationDetailExtractor extractor = new PolicyOverrideNotificationDetailExtractor();
        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);
        assertEquals(2, detailedNotificationContents.size());

        for (DetailedNotificationContent detailedNotificationContent : detailedNotificationContents) {
            Optional<String> detailedProjectName = detailedNotificationContent.getProjectName();
            assertTrue(detailedProjectName.isPresent(), "Expected project name to be present");
            assertEquals(notificationContent.getProjectName(), detailedProjectName.get());
        }

        Set<String> detailedPolicyNames = detailedNotificationContents
                                              .stream()
                                              .map(DetailedNotificationContent::getPolicyName)
                                              .flatMap(Optional::stream)
                                              .collect(Collectors.toSet());
        for (PolicyInfo policyInfo : notificationContent.getPolicyInfos()) {
            assertTrue(detailedPolicyNames.contains(policyInfo.getPolicyName()), "Expected extracted policy name to be present in notification policy infos");
        }
    }

}
