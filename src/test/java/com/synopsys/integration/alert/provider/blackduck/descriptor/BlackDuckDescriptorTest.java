package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.workflow.TaskManager;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityCollector;
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
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider, null);
        final Set<String> expectedNotificationTypes = new LinkedHashSet<>();
        expectedNotificationTypes.add(NotificationType.POLICY_OVERRIDE.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION_CLEARED.name());
        expectedNotificationTypes.add(NotificationType.VULNERABILITY.name());
        expectedNotificationTypes.add(NotificationType.LICENSE_LIMIT.name());
        final Set<String> providerNotificationTypes = descriptor.getProviderContentTypes().stream().map(contentType -> contentType.getNotificationType()).collect(Collectors.toSet());

        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

    @Test
    public void testCreateTopicCollectors() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        final BlackDuckTopicCollectorFactory topicCollectorFactory = Mockito.mock(BlackDuckTopicCollectorFactory.class);
        final List<MessageContentCollector> collectorList = Arrays.asList(Mockito.mock(BlackDuckVulnerabilityCollector.class), Mockito.mock(BlackDuckPolicyCollector.class));
        final Set<MessageContentCollector> expectedCollectorSet = new HashSet<>(collectorList);
        Mockito.when(topicCollectorFactory.createTopicCollectors()).thenReturn(expectedCollectorSet);
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, null, null, null, provider, topicCollectorFactory);
        final Set<MessageContentCollector> actualCollectorSet = descriptor.createTopicCollectors();
        Mockito.verify(topicCollectorFactory).createTopicCollectors();
        assertEquals(expectedCollectorSet, actualCollectorSet);
    }

    @Test
    public void testGetDefinedFields() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        final BlackDuckTopicCollectorFactory topicCollectorFactory = Mockito.mock(BlackDuckTopicCollectorFactory.class);
        final List<MessageContentCollector> collectorList = Arrays.asList(Mockito.mock(BlackDuckVulnerabilityCollector.class), Mockito.mock(BlackDuckPolicyCollector.class));
        final Set<MessageContentCollector> expectedCollectorSet = new HashSet<>(collectorList);
        Mockito.when(topicCollectorFactory.createTopicCollectors()).thenReturn(expectedCollectorSet);
        final BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig(provider);
        final BlackDuckProviderUIConfig blackDuckProviderUIConfig = new BlackDuckProviderUIConfig();
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(null, blackDuckProviderUIConfig, null, blackDuckDistributionUIConfig, provider, topicCollectorFactory);
        Set<DefinedFieldModel> fields = descriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL);
        assertEquals(3, fields.size());

        fields = descriptor.getAllDefinedFields(ConfigContextEnum.DISTRIBUTION);
        assertEquals(5, fields.size());
    }
}
