package com.synopsys.integration.alert.provider.polaris;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;

public class PolarisProviderTest {
    @Test
    public void initializeTest() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.unregisterTask(Mockito.anyString())).thenReturn(Optional.empty());
        final PolarisProjectSyncTask polarisProjectSyncTask = Mockito.mock(PolarisProjectSyncTask.class);
        Mockito.when(polarisProjectSyncTask.getTaskName()).thenReturn("task");
        Mockito.doNothing().when(polarisProjectSyncTask).run();
        final PolarisProvider polarisProvider = new PolarisProvider(taskManager, polarisProjectSyncTask);
        polarisProvider.initialize();
    }

    @Test
    public void destroy() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.unregisterTask(Mockito.anyString())).thenReturn(Optional.empty());
        final PolarisProjectSyncTask polarisProjectSyncTask = new PolarisProjectSyncTask(null, null, null, null, null, null);
        final PolarisProvider polarisProvider = new PolarisProvider(taskManager, polarisProjectSyncTask);
        polarisProvider.destroy();
    }

    @Disabled
    @Test
    public void getProviderContentTypesTest() {
        // TODO implement
    }

    @Test
    public void getSupportedFormatTypes() {
        final PolarisProvider polarisProvider = new PolarisProvider(null, null);
        final Set<FormatType> formatTypes = polarisProvider.getSupportedFormatTypes();

        assertTrue(formatTypes.contains(FormatType.DEFAULT));
        assertTrue(formatTypes.contains(FormatType.DIGEST));
    }
}
