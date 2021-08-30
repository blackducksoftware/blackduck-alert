package com.synopsys.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.LicenseLimitType;

public class LicenseLimitNotificationMessageExtractorTest {
    private final String summary = "License Limit Event";
    private final String description = "License Limit Message";
    private final BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();

    @Test
    public void extractTest() {
        LicenseLimitNotificationContent notificationContentComponent = createLicenseLimitNotificationContent();
        NotificationContentWrapper notificationContentWrapper = createNotificationContentWrapper(notificationContentComponent);

        LicenseLimitNotificationMessageExtractor licenseLimitNotificationMessageExtractor = new LicenseLimitNotificationMessageExtractor(blackDuckProviderKey);
        ProviderMessageHolder providerMessageHolder = licenseLimitNotificationMessageExtractor.extract(notificationContentWrapper, notificationContentComponent);

        assertEquals(0, providerMessageHolder.getProjectMessages().size());
        assertEquals(1, providerMessageHolder.getSimpleMessages().size());
        SimpleMessage simpleMessage = providerMessageHolder.getSimpleMessages().get(0);

        assertEquals(summary, simpleMessage.getSummary());
        assertEquals(description, simpleMessage.getDescription());
        assertEquals(4, simpleMessage.getDetails().size());
    }

    private NotificationContentWrapper createNotificationContentWrapper(LicenseLimitNotificationContent notificationContentComponent) {
        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(1L, 1L, "provider-test", "providerConfigName-test", "notificationType-test", "{content: \"content is here...\"}", DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), false);
        return new NotificationContentWrapper(alertNotificationModel, notificationContentComponent, LicenseLimitNotificationContent.class);
    }

    private LicenseLimitNotificationContent createLicenseLimitNotificationContent() {
        LicenseLimitNotificationContent licenseLimitNotificationContent = new LicenseLimitNotificationContent();
        licenseLimitNotificationContent.setLicenseViolationType(LicenseLimitType.MANAGED_CODEBASE_BYTES_NEW);
        licenseLimitNotificationContent.setMessage(description);
        licenseLimitNotificationContent.setMarketingPageUrl("http://someUrl");
        licenseLimitNotificationContent.setUsedCodeSize(100L);
        licenseLimitNotificationContent.setHardLimit(200L);
        licenseLimitNotificationContent.setSoftLimit(150L);

        return licenseLimitNotificationContent;
    }

}
