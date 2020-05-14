package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestConstants;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.MessageBuilderConstants;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyCommonBuilder;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.policy.PolicyOverrideMessageBuilder;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

public class PolicyOverrideMessageBuilderTest {
    private Gson gson = new Gson();

    @Test
    public void insertPolicyOverrideNotificationTest() throws Exception {
        PolicyCommonBuilder policyCommonBuilder = new PolicyCommonBuilder();
        PolicyOverrideMessageBuilder policyOverrideMessageBuilder = new PolicyOverrideMessageBuilder(policyCommonBuilder);
        runSingleTest(policyOverrideMessageBuilder, TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH);
    }

    private void runSingleTest(PolicyOverrideMessageBuilder policyOverrideMessageBuilder, String notificationJsonFileName) throws Exception {
        String content = getNotificationContentFromFile(notificationJsonFileName);
        test(policyOverrideMessageBuilder, gson.fromJson(content, PolicyOverrideNotificationView.class));
    }

    private String getNotificationContentFromFile(String notificationJsonFileName) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private void test(PolicyOverrideMessageBuilder policyOverrideMessageBuilder, PolicyOverrideNotificationView notification) {
        BlackDuckBucket blackDuckBucket = new BlackDuckBucket();
        BlackDuckServicesFactory blackDuckServicesFactory = BlackDuckMessageBuilderTestHelper.mockServicesFactory();
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(BlackDuckMessageBuilderTestHelper.mockHttpClient());

        ConfigurationJobModel job = Mockito.mock(ConfigurationJobModel.class);
        Mockito.when(job.getFieldAccessor()).thenReturn(new FieldAccessor(Map.of()));
        CommonMessageData commonMessageData = new CommonMessageData(1L, 1L, "provider", "providerConfigName", "providerUrl", OffsetDateTime.now(), job);
        List<ProviderMessageContent> messageContentGroups = policyOverrideMessageBuilder.buildMessageContents(commonMessageData, notification, blackDuckBucket, blackDuckServicesFactory);
        assertFalse(messageContentGroups.isEmpty());
        Set<String> categories = new HashSet<>();
        for (ProviderMessageContent messageContent : messageContentGroups) {
            for (ComponentItem componentItem : messageContent.getComponentItems()) {
                categories.add(componentItem.getCategory());
                assertTrue(componentItem.getComponentAttributes().stream().anyMatch(item -> item.getName().equals(MessageBuilderConstants.LABEL_POLICY_OVERRIDE_BY)));
            }
        }

        assertFalse("No ComponentItems with a category found", categories.isEmpty());
        assertEquals(1, categories.size());
        assertTrue("Policy category not found", categories.contains("Policy"));
    }

}

