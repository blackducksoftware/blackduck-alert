package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckMessageBuilderTestConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckIssueTrackerCallbackUtility;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyClearedMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyCommonBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyViolationMessageBuilder;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;

public class PolicyViolationMessageBuilderTest {
    private final Gson gson = new Gson();
    private final BlackDuckIssueTrackerCallbackUtility blackDuckIssueTrackerCallbackUtility = new BlackDuckIssueTrackerCallbackUtility(new BlackDuckProviderKey());

    @Test
    public void insertRuleViolationClearedNotificationTest() throws Exception {
        PolicyCommonBuilder policyCommonBuilder = new PolicyCommonBuilder(blackDuckIssueTrackerCallbackUtility);
        PolicyClearedMessageBuilder policyViolationClearedMessageBuilder = new PolicyClearedMessageBuilder(policyCommonBuilder);
        runSingleTest(policyViolationClearedMessageBuilder, BlackDuckMessageBuilderTestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Test
    public void insertRuleViolationNotificationTest() throws Exception {
        PolicyCommonBuilder policyCommonBuilder = new PolicyCommonBuilder(blackDuckIssueTrackerCallbackUtility);
        PolicyViolationMessageBuilder policyViolationMessageBuilder = new PolicyViolationMessageBuilder(policyCommonBuilder, blackDuckIssueTrackerCallbackUtility);
        runSingleTest(policyViolationMessageBuilder, BlackDuckMessageBuilderTestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.RULE_VIOLATION);
    }

    private void runSingleTest(BlackDuckMessageBuilder messageBuilder, String notificationJsonFileName, NotificationType notificationType) throws Exception {
        String content = getNotificationContentFromFile(notificationJsonFileName);
        NotificationView notificationView = createNotificationView(content, notificationType);
        test(messageBuilder, notificationView);
    }

    private String getNotificationContentFromFile(String notificationJsonFileName) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private NotificationView createNotificationView(String notificationContent, NotificationType notificationType) {
        if (NotificationType.RULE_VIOLATION_CLEARED == notificationType) {
            return gson.fromJson(notificationContent, RuleViolationClearedNotificationView.class);
        }
        return gson.fromJson(notificationContent, RuleViolationNotificationView.class);
    }

    private void test(BlackDuckMessageBuilder messageBuilder, NotificationView notificationView) {
        BlackDuckApiClient blackDuckApiClient = BlackDuckMessageBuilderTestHelper.mockBlackDuckApiClient();
        ProjectService projectService = BlackDuckMessageBuilderTestHelper.mockProjectService(blackDuckApiClient);
        BlackDuckServicesFactory blackDuckServicesFactory = BlackDuckMessageBuilderTestHelper.mockServicesFactory(blackDuckApiClient, projectService);

        BlackDuckHttpClient blackDuckHttpClient = BlackDuckMessageBuilderTestHelper.mockHttpClient();
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(blackDuckHttpClient);

        DistributionJobModel job = Mockito.mock(DistributionJobModel.class);
        CommonMessageData commonMessageData = new CommonMessageData(1L, 1L, "provider", "providerConfigName", "providerUrl", DateUtils.createCurrentDateTimestamp(), job);
        List<ProviderMessageContent> aggregateMessageContentList = messageBuilder.buildMessageContents(commonMessageData, notificationView, blackDuckServicesFactory);
        assertFalse(aggregateMessageContentList.isEmpty());
    }

}
