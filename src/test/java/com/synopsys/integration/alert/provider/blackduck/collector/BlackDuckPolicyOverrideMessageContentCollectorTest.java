package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestConstants;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.DefaultMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.DigestMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollapser;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckPolicyOverrideMessageContentCollectorTest {
    private final JsonExtractor jsonExtractor = new JsonExtractor(new Gson());
    private final List<MessageContentProcessor> messageContentProcessorList = Arrays.asList(new DefaultMessageContentProcessor(), new DigestMessageContentProcessor(new MessageContentCollapser()));

    @Test
    public void insertPolicyOverrideNotificationTest() throws Exception {
        final BlackDuckPolicyCollector collector = createCollector();
        runSingleTest(collector, TestConstants.POLICY_CLEARED_NOTIFICATION_JSON_PATH, NotificationType.POLICY_OVERRIDE);
    }

    @Test
    public void insertMultipleAndVerifyCorrectNumberOfCategoryItemsTest() throws Exception {
        final String topicName = "example";
        final int numberOfPoliciesOverridden = 1;
        final int policyOverlap = 1;

        // there are 4 possible linkable items per notification in the test data
        // 1- policy rule
        // 2- component
        // 3- component version or policy override user
        // 4- severity
        final int linkableItemsPerCategory = 4;

        final String overrideContent = getNotificationContentFromFile(TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH);

        final NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        final NotificationContent n1 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        final NotificationContent n2 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);

        final BlackDuckPolicyCollector collector = createCollector();

        int categoryCount = 1;
        // add 1 item for the policy override name linkable items
        final int linkableItemsCount = categoryCount * linkableItemsPerCategory;
        BlackDuckPolicyViolationMessageContentCollectorTest.insertAndAssertCountsOnTopic(collector, n0, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverridden - policyOverlap;
        BlackDuckPolicyViolationMessageContentCollectorTest.insertAndAssertCountsOnTopic(collector, n1, topicName, categoryCount, linkableItemsCount);

        categoryCount += numberOfPoliciesOverridden - policyOverlap;
        BlackDuckPolicyViolationMessageContentCollectorTest.insertAndAssertCountsOnTopic(collector, n2, topicName, categoryCount, linkableItemsCount);

        Assert.assertEquals(1, collector.collect(FormatType.DEFAULT).size());
    }

    @Test
    public void insertionExceptionTest() throws Exception {
        final BlackDuckPolicyOverrideCollector collector = createCollector();
        final BlackDuckPolicyOverrideCollector spiedCollector = Mockito.spy(collector);
        final String overrideContent = getNotificationContentFromFile(TestConstants.POLICY_OVERRIDE_NOTIFICATION_JSON_PATH);
        final NotificationContent n0 = createNotification(overrideContent, NotificationType.POLICY_OVERRIDE);
        Mockito.doThrow(new IllegalArgumentException("Insertion Error Exception Test")).when(spiedCollector)
            .addCategoryItems(Mockito.any(SortedSet.class), Mockito.any(JsonFieldAccessor.class), Mockito.anyList(), Mockito.any(NotificationContent.class));
        spiedCollector.insert(n0);
        final List<MessageContentGroup> contentList = spiedCollector.collect(FormatType.DEFAULT);
        assertTrue(contentList.isEmpty());
    }

    @Test
    public void collectEmptyMapTest() {
        final BlackDuckPolicyCollector collector = createCollector();
        final BlackDuckPolicyCollector spiedCollector = Mockito.spy(collector);
        final List<MessageContentGroup> contentList = spiedCollector.collect(FormatType.DEFAULT);
        assertTrue(contentList.isEmpty());
    }

    private void runSingleTest(final BlackDuckPolicyCollector collector, final String notificationJsonFileName, final NotificationType notificationType) throws Exception {
        final String content = getNotificationContentFromFile(notificationJsonFileName);
        final NotificationContent notificationContent = createNotification(content, notificationType);
        test(collector, notificationContent);
    }

    private BlackDuckPolicyOverrideCollector createCollector() {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any(Logger.class))).thenReturn(Optional.empty());
        return new BlackDuckPolicyOverrideCollector(jsonExtractor, messageContentProcessorList, blackDuckProperties);
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
        final List<MessageContentGroup> aggregateMessageContentList = collector.collect(FormatType.DEFAULT);
        assertFalse(aggregateMessageContentList.isEmpty());
    }

}

