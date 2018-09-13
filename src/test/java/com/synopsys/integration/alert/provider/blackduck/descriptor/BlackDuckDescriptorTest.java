package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyTopicCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityTopicCollector;
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
        final Set<String> expectedNotificationTypes = Arrays.stream(NotificationType.values()).map(NotificationType::name).collect(Collectors.toSet());
        final Set<String> providerNotificationTypes = descriptor.getProviderContentTypes().stream().map(contentType -> contentType.getNotificationType()).collect(Collectors.toSet());

        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

    @Test
    public void testCreateTopicCollectors() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask);
        final BlackDuckTopicCollectorFactory topicCollectorFactory = Mockito.mock(BlackDuckTopicCollectorFactory.class);
        final List<TopicCollector> collectorList = Arrays.asList(Mockito.mock(BlackDuckVulnerabilityTopicCollector.class), Mockito.mock(BlackDuckPolicyTopicCollector.class));
        final Set<TopicCollector> expectedCollectorSet = new HashSet<>(collectorList);
        Mockito.when(topicCollectorFactory.createTopicCollectors()).thenReturn(expectedCollectorSet);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider, topicCollectorFactory);
        final Set<TopicCollector> actualCollectorSet = descriptor.createTopicCollectors();
        Mockito.verify(topicCollectorFactory).createTopicCollectors();
        assertEquals(expectedCollectorSet, actualCollectorSet);
    }
}
