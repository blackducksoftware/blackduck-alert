package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.DefaultTopicFormatter;
import com.synopsys.integration.alert.common.workflow.processor.DigestTopicFormatter;
import com.synopsys.integration.alert.common.workflow.processor.TopicFormatter;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckPolicyTopicCollectorTest {

    private final Gson gson = new Gson();
    private final JsonExtractor jsonExtractor = new JsonExtractor(gson);
    private final List<TopicFormatter> topicFormatterList = Arrays.asList(new DefaultTopicFormatter(), new DigestTopicFormatter());

    @Test
    public void insertRuleViolationClearedNotificationTest() throws Exception {
        test("json/policyOverrideNotification.json");
    }

    @Test
    public void insertPolicyOverrideNotificationTest() throws Exception {
        test("json/policyRuleClearedNotification.json");
    }

    private void test(final String notificationJsonFileName) throws Exception {
        final BlackDuckProvider provider = new BlackDuckProvider(Mockito.mock(BlackDuckAccumulator.class));
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider);
        final BlackDuckPolicyTopicCollector collector = new BlackDuckPolicyTopicCollector(jsonExtractor, descriptor, topicFormatterList);
        final ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        final File jsonFile = classPathResource.getFile();
        final String notificationContent = FileUtils.readFileToString(jsonFile, Charset.defaultCharset());

        final NotificationContent notification = new NotificationContent(Date.from(Instant.now()), BlackDuckProvider.COMPONENT_NAME, NotificationType.RULE_VIOLATION_CLEARED.name(), notificationContent);

        collector.insert(notification);
        final List<TopicContent> topicContentList = collector.collect(FormatType.DEFAULT);
        assertFalse(topicContentList.isEmpty());
    }
}
