package com.synopsys.integration.alert.provider.blackduck.old_collector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.message.builder.BlackDuckMessageBuilderTestHelper;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckPolicyViolationMessageContentCollectorTest {
    private final JsonExtractor jsonExtractor = new JsonExtractor(new Gson());

    public static final void insertAndAssertCountsOnTopic(final BlackDuckPolicyCollector collector, final NotificationContent notification, final String topicName, final int expectedCategoryItemsCount,
        final int expectedLinkableItemsCount) {
        collector.insert(notification);
        final ProviderMessageContent content = collector.getCollectedContent()
                                                   .stream()
                                                   .filter(messageContent -> topicName.equals(messageContent.getTopic().getValue()))
                                                   .findFirst()
                                                   .orElse(null);
        final Collection<ComponentItem> items = content.getComponentItems();
        Assert.assertEquals(expectedCategoryItemsCount, items.size());
        Assert.assertEquals(expectedLinkableItemsCount, getComponentLinkableItemsCount(items));
    }

    public static int getComponentLinkableItemsCount(final Collection<ComponentItem> items) {
        int count = 0;
        for (final ComponentItem item : items) {
            count += item.getComponentAttributes().size();
        }
        return count;
    }

    @Test
    public void insertRuleViolationClearedNotificationTest() throws Exception {
        final BlackDuckPolicyCollector collector = createPolicyViolationCollector();
        runSingleTest(collector, TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Test
    public void insertRuleViolationNotificationTest() throws Exception {
        final BlackDuckPolicyCollector collector = createPolicyViolationCollector();
        runSingleTest(collector, TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.RULE_VIOLATION);
    }

    // FIXME the test is geared towards a very specific format. Since it's now more flexible, we'll have to think of a new standard to measure
    // @Test
    public void insertMultipleAndVerifyCorrectNumberOfCategoryItemsTest() throws Exception {
        final String topicName = "example";
        final int numberOfRulesCleared = 4;

        // there are 3 possible linkable items per notification in the test data
        // 1- policy rule
        // 2- component
        // 3- component version or policy override user
        final int linkableItemsPerCategory = 3;

        final String ruleContent = getNotificationContentFromFile(TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH);

        final NotificationContent n0 = createNotification(ruleContent, NotificationType.RULE_VIOLATION_CLEARED);
        final NotificationContent n1 = createNotification(ruleContent, NotificationType.RULE_VIOLATION_CLEARED);

        final BlackDuckPolicyCollector collector = createPolicyViolationCollector();

        //Rules are now being combined to fix size
        int categoryCount = linkableItemsPerCategory;
        // add 1 item for the policy override name linkable items
        int linkableItemsCount = categoryCount * linkableItemsPerCategory;
        insertAndAssertCountsOnTopic(collector, n0, topicName, categoryCount, linkableItemsCount);

        categoryCount = numberOfRulesCleared;
        linkableItemsCount = categoryCount * linkableItemsPerCategory;
        insertAndAssertCountsOnTopic(collector, n1, topicName, categoryCount, linkableItemsCount);

        Assert.assertEquals(1, collector.getCollectedContent().size());
    }

    @Test
    public void testOperationCircuitBreaker() throws Exception {
        String ruleContent = getNotificationContentFromFile(TestConstants.NOTIFICATION_JSON_PATH);
        NotificationContent n0 = createNotification(ruleContent, NotificationType.BOM_EDIT);
        BlackDuckPolicyCollector collector = createPolicyViolationCollector();
        collector.insert(n0);
        Assert.assertEquals(0, collector.getCollectedContent().size());
    }

    @Test
    public void insertionExceptionTest() throws Exception {
        BlackDuckPolicyViolationCollector collector = createPolicyViolationCollector();
        BlackDuckPolicyViolationCollector spiedCollector = Mockito.spy(collector);
        String overrideContent = getNotificationContentFromFile(TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH);
        NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        Mockito.doThrow(new IllegalArgumentException("Insertion Error Exception Test")).when(spiedCollector)
            .getComponentItems(Mockito.any(JsonFieldAccessor.class), Mockito.anyList(), Mockito.any(NotificationContent.class));
        spiedCollector.insert(n0);
        List<ProviderMessageContent> contentList = spiedCollector.getCollectedContent();
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void collectEmptyMapTest() {
        BlackDuckPolicyCollector collector = createPolicyViolationCollector();
        BlackDuckPolicyCollector spiedCollector = Mockito.spy(collector);
        List<ProviderMessageContent> contentList = spiedCollector.getCollectedContent();
        assertTrue(contentList.isEmpty());
    }

    private void runSingleTest(BlackDuckPolicyCollector collector, String notificationJsonFileName, NotificationType notificationType) throws Exception {
        String content = getNotificationContentFromFile(TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH);
        NotificationContent notificationContent = createNotification(content, notificationType);
        test(collector, notificationContent);
    }

    private BlackDuckPolicyViolationCollector createPolicyViolationCollector() {
        BlackDuckProperties blackDuckProperties = BlackDuckMessageBuilderTestHelper.mockProperties();
        return new BlackDuckPolicyViolationCollector(jsonExtractor, blackDuckProperties);
    }

    private String getNotificationContentFromFile(String notificationJsonFileName) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private NotificationContent createNotification(String notificationContent, NotificationType type) {
        Date creationDate = Date.from(Instant.now());
        return new NotificationContent(creationDate, new BlackDuckProviderKey().getUniversalKey(), creationDate, type.name(), notificationContent);
    }

    private void test(BlackDuckPolicyCollector collector, NotificationContent notification) {
        collector.insert(notification);
        List<ProviderMessageContent> aggregateMessageContentList = collector.getCollectedContent();
        assertFalse(aggregateMessageContentList.isEmpty());
    }

}
