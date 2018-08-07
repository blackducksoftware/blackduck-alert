package com.blackducksoftware.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackducksoftware.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

public class BlackDuckDescriptorTest {

    @Test
    public void testGetProvider() {
        final BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, provider);
        assertEquals(provider, descriptor.getProvider());
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, provider);
        final Set<String> expectedNotificationTypes = Arrays.stream(NotificationType.values()).map(NotificationType::name).collect(Collectors.toSet());
        final Set<String> providerNotificationTypes = descriptor.getNotificationTypes();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }
}
