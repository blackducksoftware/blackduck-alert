package com.synopsys.integration.alert.provider.blackduck.new_collector;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

public class LicenseLimitMessageBuilder implements BlackDuckMessageBuilder<LicenseLimitNotificationView> {
    private final Logger logger = LoggerFactory.getLogger(LicenseLimitMessageBuilder.class);

    @Override
    public String getNotificationType() {
        return NotificationType.LICENSE_LIMIT.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(Long notificationId, Date providerCreationDate, ConfigurationJobModel job, LicenseLimitNotificationView notificationView, BlackDuckBucket blackDuckBucket,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        LicenseLimitNotificationContent notificationContent = notificationView.getContent();
        try {
            String usageMessage = createUsageMessage(notificationContent);
            ProviderMessageContent.Builder projectMessageBuilder = new ProviderMessageContent.Builder()
                                                                       .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                       .applyTopic("License Limit Message", notificationContent.getMessage())
                                                                       .applySubTopic("Usage Info", usageMessage)
                                                                       .applyAction(ItemOperation.INFO)
                                                                       .applyNotificationId(notificationId)
                                                                       .applyProviderCreationTime(providerCreationDate);
            return List.of(projectMessageBuilder.build());
        } catch (AlertException e) {
            logger.error("Unable to build Project notification messages", e);
            return List.of();
        }
    }

    private String createUsageMessage(LicenseLimitNotificationContent notificationContent) {
        return String.format("Used Code Size: %d, Hard Limit: %d, Soft Limit: %d", notificationContent.getUsedCodeSize(), notificationContent.getHardLimit(), notificationContent.getSoftLimit());
    }

}
