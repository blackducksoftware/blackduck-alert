package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.workflow.processor.DefaultMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.DigestMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckPolicyMessageContentCollectorTest {
    private final JsonExtractor jsonExtractor = new JsonExtractor(new Gson());
    private final List<MessageContentProcessor> messageContentProcessorList = Arrays.asList(new DefaultMessageContentProcessor(), new DigestMessageContentProcessor());

    @Test
    public void insertRuleViolationClearedNotificationTest() throws Exception {
        final BlackDuckPolicyMessageContentCollector collector = createCollector();
        runSingleTest(collector, "json/policyRuleClearedNotification.json", NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Test
    public void insertPolicyOverrideNotificationTest() throws Exception {
        final BlackDuckPolicyMessageContentCollector collector = createCollector();
        runSingleTest(collector, "json/policyOverrideNotification.json", NotificationType.POLICY_OVERRIDE);
    }

    @Test
    public void insertMultipleAndVerifyCorrectNumberOfCategoryItemsTest() throws Exception {
        final String topicName = "example";
        final int numberOfRulesCleared = 3;
        final int numberOrRuleComponents = 1;
        final int numberOfPoliciesOverriden = 1;
        final int numberOfPolicyOverrideComponents = 1;
        final int policyOverlap = 1;

        final String ruleContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final String overrideContent = getNotificationContentFromFile("json/policyOverrideNotification.json");

        final NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        final NotificationContent n1 = createNotification(ruleContent, NotificationType.RULE_VIOLATION_CLEARED);
        final NotificationContent n2 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        final NotificationContent n3 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);

        final BlackDuckPolicyMessageContentCollector collector = createCollector();

        int categoryCount = numberOfPoliciesOverriden;
        // add 1 item for the policy override name linkable items
        int linkableItemsCount = numberOfPoliciesOverriden * numberOfPolicyOverrideComponents * 3;
        insertAndAssertCountsOnTopic(collector, n0, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfRulesCleared;
        linkableItemsCount += numberOfRulesCleared * numberOrRuleComponents * 2;
        insertAndAssertCountsOnTopic(collector, n1, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverriden - policyOverlap;
        insertAndAssertCountsOnTopic(collector, n2, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverriden - policyOverlap;
        insertAndAssertCountsOnTopic(collector, n3, topicName, categoryCount, linkableItemsCount);

        Assert.assertEquals(1, collector.collect(FormatType.DEFAULT).size());
    }

    @Test
    public void insertionExceptionTest() throws Exception {
        final BlackDuckPolicyMessageContentCollector collector = createCollector();
        final BlackDuckPolicyMessageContentCollector spiedCollector = Mockito.spy(collector);
        final String overrideContent = getNotificationContentFromFile("json/policyOverrideNotification.json");
        final NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        Mockito.doThrow(new IllegalArgumentException("Insertion Error Exception Test")).when(spiedCollector)
            .addCategoryItems(Mockito.anyList(), Mockito.any(JsonFieldAccessor.class), Mockito.anyList(), Mockito.any(NotificationContent.class));
        spiedCollector.insert(n0);
        final List<AggregateMessageContent> contentList = spiedCollector.collect(FormatType.DEFAULT);
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void collectEmptyMapTest() {
        final BlackDuckPolicyMessageContentCollector collector = createCollector();
        final BlackDuckPolicyMessageContentCollector spiedCollector = Mockito.spy(collector);
        final List<AggregateMessageContent> contentList = spiedCollector.collect(FormatType.DEFAULT);
        assertTrue(contentList.isEmpty());
    }

    private void runSingleTest(final BlackDuckPolicyMessageContentCollector collector, final String notificationJsonFileName, final NotificationType notificationType) throws Exception {
        final String content = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent notificationContent = createNotification(content, notificationType);
        test(collector, notificationContent);
    }

    private BlackDuckPolicyMessageContentCollector createCollector() {
        return new BlackDuckPolicyMessageContentCollector(jsonExtractor, messageContentProcessorList);
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

    private void test(final BlackDuckPolicyMessageContentCollector collector, final NotificationContent notification) {
        collector.insert(notification);
        final List<AggregateMessageContent> aggregateMessageContentList = collector.collect(FormatType.DEFAULT);
        assertFalse(aggregateMessageContentList.isEmpty());
    }

    private void insertAndAssertCountsOnTopic(final BlackDuckPolicyMessageContentCollector collector, final NotificationContent notification, final String topicName, final int expectedCategoryItemsCount,
        final int expectedLinkableItemsCount) {
        collector.insert(notification);
        final AggregateMessageContent content = collector.collect(FormatType.DEFAULT).stream().filter(topicContent -> topicName.equals(topicContent.getValue())).findFirst().orElse(null);
        final List<CategoryItem> items = content.getCategoryItemList();
        Assert.assertEquals(expectedCategoryItemsCount, items.size());
        Assert.assertEquals(expectedLinkableItemsCount, getCategoryItemLinkableItemsCount(items));
    }

    private int getCategoryItemLinkableItemsCount(final List<CategoryItem> items) {
        int count = 0;
        for (final CategoryItem item : items) {
            count += item.getItems().size();
        }
        return count;
    }
}
