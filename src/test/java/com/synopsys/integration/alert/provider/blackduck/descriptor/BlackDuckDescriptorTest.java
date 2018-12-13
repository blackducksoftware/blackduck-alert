package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.ProjectSyncTask;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckDescriptorTest {

    @Test
    public void testGetProvider() {
        final BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider, null);
        assertEquals(provider, descriptor.getProvider());
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider, null);
        final Set<String> expectedNotificationTypes = new LinkedHashSet<>();
        expectedNotificationTypes.add(NotificationType.LICENSE_LIMIT.name());
        expectedNotificationTypes.add(NotificationType.POLICY_OVERRIDE.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION_CLEARED.name());
        expectedNotificationTypes.add(NotificationType.VULNERABILITY.name());
        final Set<String> providerNotificationTypes = descriptor.getProviderContentTypes().stream().map(contentType -> contentType.getNotificationType()).collect(Collectors.toSet());

        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

    @Test
    public void testCreateTopicCollectors() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask);
        final BlackDuckTopicCollectorFactory topicCollectorFactory = Mockito.mock(BlackDuckTopicCollectorFactory.class);
        final List<MessageContentCollector> collectorList = Arrays.asList(Mockito.mock(BlackDuckVulnerabilityMessageContentCollector.class), Mockito.mock(BlackDuckPolicyMessageContentCollector.class));
        final Set<MessageContentCollector> expectedCollectorSet = new HashSet<>(collectorList);
        Mockito.when(topicCollectorFactory.createTopicCollectors()).thenReturn(expectedCollectorSet);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider, topicCollectorFactory);
        final Set<MessageContentCollector> actualCollectorSet = descriptor.createTopicCollectors();
        Mockito.verify(topicCollectorFactory).createTopicCollectors();
        assertEquals(expectedCollectorSet, actualCollectorSet);
    }
}
