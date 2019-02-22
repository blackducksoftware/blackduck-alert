package com.synopsys.integration.alert.provider.blackduck.collector;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.workflow.filter.builder.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.processor.DefaultMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.DigestMessageContentProcessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.LicenseLimitType;

public class BlackDuckLicenseLimitCollectorTest {
    private final Gson gson = new Gson();
    private final List<MessageContentProcessor> messageContentProcessorList = Arrays.asList(new DefaultMessageContentProcessor(), new DigestMessageContentProcessor());

    @Test
    public void addCategoryItemsTest() {
        final JsonExtractor jsonExtractor = new JsonExtractor(gson);
        final BlackDuckLicenseLimitCollector collector = new BlackDuckLicenseLimitCollector(jsonExtractor, messageContentProcessorList);
        final NotificationContent notification = getNotificationContent();

        collector.insert(notification);
        final List<AggregateMessageContent> aggregateMessageContentList = collector.collect(FormatType.DEFAULT);

        assertEquals(1, aggregateMessageContentList.size());
    }

    private NotificationContent getNotificationContent() {
        final LicenseLimitNotificationContent content = new LicenseLimitNotificationContent();
        content.setLicenseViolationType(LicenseLimitType.MANAGED_CODEBASE_BYTES_NEW);
        content.setMarketingPageUrl("https://google.com");
        content.setMessage("Unit test message");
        content.setUsedCodeSize(81L);
        content.setHardLimit(100L);
        content.setSoftLimit(80L);

        final String notification = String.format("{\"content\":%s}", gson.toJson(content));
        final NotificationContent notificationContent = new NotificationContent(new Date(), BlackDuckProvider.COMPONENT_NAME, new Date(), NotificationType.LICENSE_LIMIT.name(), notification);
        notificationContent.setId(1L);
        return notificationContent;
    }
}
