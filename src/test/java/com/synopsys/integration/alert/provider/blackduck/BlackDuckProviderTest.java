package com.synopsys.integration.alert.provider.blackduck;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.workflow.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.ProjectSyncTask;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class BlackDuckProviderTest {

    @Test
    public void testInitialize() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(ProjectSyncTask.TASK_NAME);
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        provider.initialize();
        Mockito.verify(taskManager, Mockito.times(2)).scheduleCronTask(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDestroy() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(ProjectSyncTask.TASK_NAME);
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        provider.destroy();
        Mockito.verify(taskManager, Mockito.times(2)).unregisterTask(Mockito.anyString());
    }

    @Test
    public void testGetNotificationTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        final Set<String> expectedNotificationTypes = new LinkedHashSet<>();
        expectedNotificationTypes.add(NotificationType.POLICY_OVERRIDE.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION.name());
        expectedNotificationTypes.add(NotificationType.RULE_VIOLATION_CLEARED.name());
        expectedNotificationTypes.add(NotificationType.VULNERABILITY.name());
        final Set<String> providerNotificationTypes = provider.getProviderContentTypes().stream().map(contentType -> contentType.getNotificationType()).collect(Collectors.toSet());
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

    @Test
    public void testGetSupportedFormatTypes() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final ProjectSyncTask projectSyncTask = Mockito.mock(ProjectSyncTask.class);
        final TaskManager taskManager = new TaskManager();
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask, projectSyncTask, null, taskManager);
        final Set<FormatType> expectedNotificationTypes = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST);
        final Set<FormatType> providerNotificationTypes = provider.getSupportedFormatTypes();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }
}
