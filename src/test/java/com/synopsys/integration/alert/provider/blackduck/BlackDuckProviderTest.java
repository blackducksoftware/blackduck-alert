package com.synopsys.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckProjectSyncTask;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckProviderTest {

    @Test
    public void testInitializeNotConfigured() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProjectSyncTask projectSyncTask = Mockito.mock(BlackDuckProjectSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(BlackDuckProjectSyncTask.TASK_NAME);
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfigSafely(Mockito.any(IntLogger.class))).thenReturn(Optional.empty());
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties);
        provider.initialize();
        Mockito.verify(taskManager, Mockito.times(0)).scheduleCronTask(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testInitializeConfigured() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProjectSyncTask projectSyncTask = Mockito.mock(BlackDuckProjectSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(BlackDuckProjectSyncTask.TASK_NAME);
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfigSafely(Mockito.any(IntLogger.class))).thenReturn(Optional.of(Mockito.mock(BlackDuckServerConfig.class)));
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties);
        provider.initialize();
        Mockito.verify(taskManager, Mockito.times(2)).scheduleCronTask(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDestroy() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProjectSyncTask projectSyncTask = Mockito.mock(BlackDuckProjectSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(BlackDuckProjectSyncTask.TASK_NAME);
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties);
        provider.destroy();
        Mockito.verify(taskManager, Mockito.times(2)).unregisterTask(Mockito.anyString());
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProjectSyncTask projectSyncTask = Mockito.mock(BlackDuckProjectSyncTask.class);
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties);
        final Set<String> expectedNotificationTypes = new LinkedHashSet<>();
        expectedNotificationTypes.add(NotificationType.POLICY_OVERRIDE.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION_CLEARED.name());
        expectedNotificationTypes.add(NotificationType.VULNERABILITY.name());
        expectedNotificationTypes.add(NotificationType.LICENSE_LIMIT.name());
        final Set<String> providerNotificationTypes = provider.getProviderContentTypes().stream().map(contentType -> contentType.getNotificationType()).collect(Collectors.toSet());
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

    @Test
    public void testGetSupportedFormatTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProjectSyncTask projectSyncTask = Mockito.mock(BlackDuckProjectSyncTask.class);
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties);
        final Set<FormatType> expectedNotificationTypes = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);
        final Set<FormatType> providerNotificationTypes = provider.getSupportedFormatTypes();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }
}
