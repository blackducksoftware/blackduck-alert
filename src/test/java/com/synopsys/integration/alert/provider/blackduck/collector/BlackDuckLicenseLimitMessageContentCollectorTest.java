package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.workflow.processor.DefaultMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.DigestMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.blackduck.api.enumeration.LicenseLimitType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.notification.content.LicenseLimitNotificationContent;

public class BlackDuckLicenseLimitMessageContentCollectorTest {
    private final Gson gson = new Gson();
    private final JsonExtractor jsonExtractor = new JsonExtractor(gson);
    private final List<MessageContentProcessor> messageContentProcessorList = Arrays.asList(new DefaultMessageContentProcessor(), new DigestMessageContentProcessor());

    @Test
    public void addCategoryItemsTest() {
        final JsonExtractor jsonExtractor = new JsonExtractor(gson);
        final BlackDuckLicenseLimitMessageContentCollector collector = new BlackDuckLicenseLimitMessageContentCollector(jsonExtractor, messageContentProcessorList);
        final NotificationContent notification = getNotificationContent();

        collector.insert(notification);
        final List<AggregateMessageContent> aggregateMessageContentList = collector.collect(FormatType.DEFAULT);

        assertEquals(1, aggregateMessageContentList.size());
    }

    private NotificationContent getNotificationContent() {
        final LicenseLimitNotificationContent content = new LicenseLimitNotificationContent();
        content.licenseViolationType = LicenseLimitType.MANAGED_CODEBASE_BYTES_NEW;
        content.marketingPageUrl = "https://google.com";
        content.message = "Unit test message";
        content.usedCodeSize = 81L;
        content.hardLimit = 100L;
        content.softLimit = 80L;

        final String notification = String.format("{\"content\":%s}", gson.toJson(content));
        final NotificationContent notificationContent = new NotificationContent(new Date(), BlackDuckProvider.COMPONENT_NAME, new Date(), NotificationType.LICENSE_LIMIT.name(), notification);
        notificationContent.setId(1L);
        return notificationContent;
    }
}
