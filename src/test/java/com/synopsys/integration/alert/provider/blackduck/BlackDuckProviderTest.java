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
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckDataSyncTask;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckProviderTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Test
    public void testInitializeNotConfigured() {
        BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        BlackDuckDataSyncTask projectSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(BlackDuckDataSyncTask.TASK_NAME);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfigSafely(Mockito.any(IntLogger.class))).thenReturn(Optional.empty());
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties, null, null);
        provider.initialize();
        Mockito.verify(taskManager, Mockito.times(0)).scheduleCronTask(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testInitializeConfigured() {
        BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        BlackDuckDataSyncTask projectSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(BlackDuckDataSyncTask.TASK_NAME);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfigSafely(Mockito.any(IntLogger.class))).thenReturn(Optional.of(Mockito.mock(BlackDuckServerConfig.class)));
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties, null, null);
        provider.initialize();
        Mockito.verify(taskManager, Mockito.times(2)).scheduleCronTask(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDestroy() {
        BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        BlackDuckDataSyncTask projectSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);
        Mockito.when(accumulatorTask.getTaskName()).thenReturn(BlackDuckAccumulator.TASK_NAME);
        Mockito.when(projectSyncTask.getTaskName()).thenReturn(BlackDuckDataSyncTask.TASK_NAME);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, null, taskManager, blackDuckProperties, null, null);
        provider.destroy();
        Mockito.verify(taskManager, Mockito.times(2)).unregisterTask(Mockito.anyString());
    }

    @Test
    public void testGetNotificationTypes() {
        BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        BlackDuckDataSyncTask projectSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);
        TaskManager taskManager = new TaskManager();
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, blackDuckContent, taskManager, blackDuckProperties, null, null);
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
        TaskManager taskManager = new TaskManager();
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckProvider provider = new BlackDuckProvider(BLACK_DUCK_PROVIDER_KEY, accumulatorTask, projectSyncTask, blackDuckContent, taskManager, blackDuckProperties, null, null);
        Set<FormatType> expectedNotificationTypes = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);
        Set<FormatType> providerNotificationTypes = provider.getProviderContent().getSupportedContentFormats();
        assertEquals(expectedNotificationTypes, providerNotificationTypes);
    }

}
