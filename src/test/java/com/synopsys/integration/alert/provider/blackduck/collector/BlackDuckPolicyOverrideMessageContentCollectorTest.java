package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestConstants;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckPolicyOverrideMessageContentCollectorTest {
    private final JsonExtractor jsonExtractor = new JsonExtractor(new Gson());

    @Test
    public void insertPolicyOverrideNotificationTest() throws Exception {
        final BlackDuckPolicyCollector collector = createCollector();
        runSingleTest(collector, TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH, NotificationType.POLICY_OVERRIDE);
    }

    @Test
    public void insertMultipleAndVerifyCorrectNumberOfCategoryItemsTest() throws Exception {
        final String topicName = "example";
        final int numberOfPoliciesOverridden = 1;

        // there are 3 possible linkable items per notification in the test data
        // 1- policy rule
        // 2- policy override user
        // 3- severity
        final int linkableItemsPerCategory = 3;

        final String overrideContent = getNotificationContentFromFile(TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH);

        final NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        final NotificationContent n1 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        final NotificationContent n2 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);

        n0.setId(1L);
        n1.setId(2L);
        n2.setId(3L);

        final BlackDuckPolicyCollector collector = createCollector();

        int categoryCount = 1;
        // add 1 item for the policy override name linkable items
        int linkableItemsCount = categoryCount * linkableItemsPerCategory;
        BlackDuckPolicyViolationMessageContentCollectorTest.insertAndAssertCountsOnTopic(collector, n0, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverridden;
        linkableItemsCount = categoryCount * linkableItemsPerCategory;
        BlackDuckPolicyViolationMessageContentCollectorTest.insertAndAssertCountsOnTopic(collector, n1, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverridden;
        linkableItemsCount = categoryCount * linkableItemsPerCategory;
        BlackDuckPolicyViolationMessageContentCollectorTest.insertAndAssertCountsOnTopic(collector, n2, topicName, categoryCount, linkableItemsCount);

        Assert.assertEquals(1, collector.getCollectedContent().size());
    }

    @Test
    public void insertionExceptionTest() throws Exception {
        final BlackDuckPolicyOverrideCollector collector = createCollector();
        final BlackDuckPolicyOverrideCollector spiedCollector = Mockito.spy(collector);
        final String overrideContent = getNotificationContentFromFile(TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH);
        final NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        Mockito.doThrow(new IllegalArgumentException("Insertion Error Exception Test")).when(spiedCollector)
            .getComponentItems(Mockito.any(JsonFieldAccessor.class), Mockito.anyList(), Mockito.any(NotificationContent.class));
        spiedCollector.insert(n0);
        final List<ProviderMessageContent> contentList = spiedCollector.getCollectedContent();
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void collectEmptyMapTest() {
        final BlackDuckPolicyCollector collector = createCollector();
        final BlackDuckPolicyCollector spiedCollector = Mockito.spy(collector);
        final List<ProviderMessageContent> contentList = spiedCollector.getCollectedContent();
        assertTrue(contentList.isEmpty());
    }

    private void runSingleTest(final BlackDuckPolicyCollector collector, final String notificationJsonFileName, final NotificationType notificationType) throws Exception {
        final String content = getNotificationContentFromFile(notificationJsonFileName);
        final NotificationContent notificationContent = createNotification(content, notificationType);
        notificationContent.setId(1L);
        test(collector, notificationContent);
    }

    private BlackDuckPolicyOverrideCollector createCollector() {
        final BlackDuckProperties blackDuckProperties = BlackDuckCollectorTestHelper.mockProperties();
        return new BlackDuckPolicyOverrideCollector(jsonExtractor, blackDuckProperties);
    }

    private String getNotificationContentFromFile(final String notificationJsonFileName) throws Exception {
        final ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        final File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private NotificationContent createNotification(final String notificationContent, final NotificationType type) {
        final Date creationDate = Date.from(Instant.now());
        return new NotificationContent(creationDate, BlackDuckProvider.COMPONENT_NAME, creationDate, type.name(), notificationContent);
    }

    private void test(final BlackDuckPolicyCollector collector, final NotificationContent notification) {
        collector.insert(notification);
        final List<ProviderMessageContent> messageContentGroups = collector.getCollectedContent();
        assertFalse(messageContentGroups.isEmpty());
        Set<String> categories = new HashSet<>();
        for (ProviderMessageContent messageContent : messageContentGroups) {
            for (ComponentItem componentItem : messageContent.getComponentItems()) {
                categories.add(componentItem.getCategory());
                assertTrue(componentItem.getComponentAttributes().stream().anyMatch(item -> item.getName().equals(BlackDuckContent.LABEL_POLICY_OVERRIDE_BY)));
            }
        }

        assertFalse("No ComponentItems with a category found", categories.isEmpty());
        assertEquals(1, categories.size());
        assertTrue("Policy category not found", categories.contains("Policy"));
    }
}

