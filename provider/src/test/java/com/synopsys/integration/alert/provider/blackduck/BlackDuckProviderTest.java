package com.synopsys.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckTaskFactory;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class BlackDuckProviderTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Test
    public void testGetNotificationTypes() {
        BlackDuckTaskFactory taskFactory = Mockito.mock(BlackDuckTaskFactory.class);
        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, blackDuckContent, null, null, taskFactory);
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
        BlackDuckTaskFactory taskFactory = Mockito.mock(BlackDuckTaskFactory.class);
        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, blackDuckContent, null, null, taskFactory);
        Set<ProcessingType> expectedNotificationTypes = EnumSet.of(ProcessingType.DEFAULT, ProcessingType.DIGEST, ProcessingType.SUMMARY);
        Set<ProcessingType> providerNotificationTypes = provider.getProviderContent().getSupportedProcessingTypes();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

}
