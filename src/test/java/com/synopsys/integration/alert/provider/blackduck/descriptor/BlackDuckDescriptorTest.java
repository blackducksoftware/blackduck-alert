package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.EmailSyncTask;
import com.synopsys.integration.alert.provider.blackduck.tasks.GroupSyncTask;
import com.synopsys.integration.alert.provider.blackduck.tasks.ProjectSyncTask;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckDescriptorTest {

    @Test
    public void testGetProvider() {
        final BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider);
        assertEquals(provider, descriptor.getProvider());
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final EmailSyncTask emailSyncTask = Mockito.mock(EmailSyncTask.class);
        final GroupSyncTask groupSyncTask = Mockito.mock(GroupSyncTask.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, emailSyncTask, groupSyncTask, projectSyncTask);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider);
        final Set<String> expectedNotificationTypes = Arrays.stream(NotificationType.values()).map(NotificationType::name).collect(Collectors.toSet());
        final Set<String> providerNotificationTypes = descriptor.getProviderContentTypes().stream().map(contentType -> contentType.getNotificationType()).collect(Collectors.toSet());

        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }
}
