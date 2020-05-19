package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestConstants;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyClearedMessageBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyCommonBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyViolationMessageBuilder;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

public class PolicyViolationMessageBuilderTest {
    private Gson gson = new Gson();

    @Test
    public void insertRuleViolationClearedNotificationTest() throws Exception {
        PolicyCommonBuilder policyCommonBuilder = new PolicyCommonBuilder();
        PolicyClearedMessageBuilder policyViolationClearedMessageBuilder = new PolicyClearedMessageBuilder(policyCommonBuilder);
        runSingleTest(policyViolationClearedMessageBuilder, TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Test
    public void insertRuleViolationNotificationTest() throws Exception {
        PolicyCommonBuilder policyCommonBuilder = new PolicyCommonBuilder();
        PolicyViolationMessageBuilder policyViolationMessageBuilder = new PolicyViolationMessageBuilder(policyCommonBuilder);
        runSingleTest(policyViolationMessageBuilder, TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.RULE_VIOLATION);
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
        BlackDuckBucket blackDuckBucket = new BlackDuckBucket();
        BlackDuckServicesFactory blackDuckServicesFactory = BlackDuckMessageBuilderTestHelper.mockServicesFactory();

        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(BlackDuckMessageBuilderTestHelper.mockHttpClient());

        ConfigurationJobModel job = Mockito.mock(ConfigurationJobModel.class);
        Mockito.when(job.getFieldAccessor()).thenReturn(new FieldAccessor(Map.of()));
        CommonMessageData commonMessageData = new CommonMessageData(1L, 1L, "provider", "providerConfigName", "providerUrl", DateUtils.createCurrentDateTimestamp(), job);
        List<ProviderMessageContent> aggregateMessageContentList = messageBuilder.buildMessageContents(commonMessageData, notificationView, blackDuckBucket, blackDuckServicesFactory);
        assertFalse(aggregateMessageContentList.isEmpty());
    }

}
