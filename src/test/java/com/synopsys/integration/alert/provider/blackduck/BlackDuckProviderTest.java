package com.synopsys.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckDataSyncTask;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class BlackDuckProviderTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Test
    public void testGetNotificationTypes() {
        BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        BlackDuckDataSyncTask projectSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);

        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, blackDuckContent, null, null);
        Set<String> expectedNotificationTypes = new LinkedHashSet<>();
        expectedNotificationTypes.add(NotificationType.POLICY_OVERRIDE.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION_CLEARED.name());
        expectedNotificationTypes.add(NotificationType.VULNERABILITY.name());
        expectedNotificationTypes.add(NotificationType.LICENSE_LIMIT.name());
        expectedNotificationTypes.add(NotificationType.BOM_EDIT.name());
        expectedNotificationTypes.add(NotificationType.PROJECT.name());
        expectedNotificationTypes.add(NotificationType.PROJECT_VERSION.name());
        Set<String> providerNotificationTypes = provider.getProviderContent().getContentTypes().stream().map(ProviderNotificationType::getNotificationType).collect(Collectors.toSet());
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

    @Test
    public void testGetSupportedFormatTypes() {
        BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        BlackDuckDataSyncTask projectSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);

        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, blackDuckContent, null, null);
        Set<FormatType> expectedNotificationTypes = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);
        Set<FormatType> providerNotificationTypes = provider.getProviderContent().getSupportedContentFormats();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

}
