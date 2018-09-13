package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.DefaultTopicFormatter;
import com.synopsys.integration.alert.common.workflow.processor.DigestTopicFormatter;
import com.synopsys.integration.alert.common.workflow.processor.TopicFormatter;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.ProjectSyncTask;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckPolicyTopicCollectorTest {

    private final Gson gson = new Gson();
    private final JsonExtractor jsonExtractor = new JsonExtractor(gson);
    private final List<TopicFormatter> topicFormatterList = Arrays.asList(new DefaultTopicFormatter(), new DigestTopicFormatter());

    @Test
    public void insertRuleViolationClearedNotificationTest() throws Exception {
        final BlackDuckPolicyTopicCollector collector = createCollector();
        runSingleTest(collector, "json/policyRuleClearedNotification.json", NotificationType.RULE_VIOLATION_CLEARED);
    }

    @Test
    public void insertPolicyOverrideNotificationTest() throws Exception {
        final BlackDuckPolicyTopicCollector collector = createCollector();
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

        final BlackDuckPolicyTopicCollector collector = createCollector();

        int categoryCount = numberOfPoliciesOverriden;
        int linkableItemsCount = numberOfPoliciesOverriden * numberOfPolicyOverrideComponents * 2;
        insertAndAssertCountsOnTopic(collector, n0, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfRulesCleared;
        linkableItemsCount += numberOfRulesCleared * numberOrRuleComponents * 2;
        insertAndAssertCountsOnTopic(collector, n1, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverriden - policyOverlap;
        linkableItemsCount += numberOfPoliciesOverriden * numberOfPolicyOverrideComponents * 2;
        insertAndAssertCountsOnTopic(collector, n2, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverriden - policyOverlap;
        linkableItemsCount += numberOfPoliciesOverriden * numberOfPolicyOverrideComponents * 2;
        insertAndAssertCountsOnTopic(collector, n3, topicName, categoryCount, linkableItemsCount);

        Assert.assertEquals(1, collector.collect(FormatType.DEFAULT).size());
    }

    private void runSingleTest(final BlackDuckPolicyTopicCollector collector, final String notificationJsonFileName, final NotificationType notificationType) throws Exception {
        final String content = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final NotificationContent notificationContent = createNotification(content, notificationType);
        test(collector, notificationContent);
    }

    private BlackDuckPolicyTopicCollector createCollector() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider);
        return new BlackDuckPolicyTopicCollector(jsonExtractor, descriptor, topicFormatterList);
    }

    private String getNotificationContentFromFile(final String notificationJsonFileName) throws Exception {
        final ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        final File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private NotificationContent createNotification(final String notificationContent, final NotificationType type) {
        return new NotificationContent(Date.from(Instant.now()), BlackDuckProvider.COMPONENT_NAME, type.name(), notificationContent);
    }

    private void test(final BlackDuckPolicyTopicCollector collector, final NotificationContent notification) {
        collector.insert(notification);
        final List<TopicContent> topicContentList = collector.collect(FormatType.DEFAULT);
        assertFalse(topicContentList.isEmpty());
    }

    private void insertAndAssertCountsOnTopic(final BlackDuckPolicyTopicCollector collector, final NotificationContent notification, final String topicName, final int expectedCategoryItemsCount, final int expectedLinkableItemsCount) {
        collector.insert(notification);
        final TopicContent content = collector.collect(FormatType.DEFAULT).stream().filter(topicContent -> topicName.equals(topicContent.getValue())).findFirst().orElse(null);
        final List<CategoryItem> items = content.getCategoryItemList();
        Assert.assertEquals(expectedCategoryItemsCount, items.size());
        Assert.assertEquals(expectedLinkableItemsCount, getCategoryItemLinkableItemsCount(items));
    }

    private int getCategoryItemLinkableItemsCount(final List<CategoryItem> items) {
        int count = 0;
        for (final CategoryItem item : items) {
            count += item.getItemList().size();
        }
        return count;
    }
}
