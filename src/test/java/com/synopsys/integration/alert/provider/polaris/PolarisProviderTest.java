package com.synopsys.integration.alert.provider.polaris;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;

public class PolarisProviderTest {
    @Test
    public void initializeTest() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final PolarisProjectSyncTask polarisProjectSyncTask = Mockito.mock(PolarisProjectSyncTask.class);
        final PolarisProperties polarisProperties = Mockito.mock(PolarisProperties.class);

        Mockito.when(taskManager.unregisterTask(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(polarisProjectSyncTask.getTaskName()).thenReturn("task");
        Mockito.doNothing().when(polarisProjectSyncTask).run();
        Mockito.when(polarisProperties.createPolarisHttpClientSafely((Logger) Mockito.any())).thenReturn(Optional.empty());

        final PolarisProvider polarisProvider = new PolarisProvider(taskManager, polarisProjectSyncTask, polarisProperties, null);
        polarisProvider.initialize();
    }

    @Test
    public void destroy() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.unregisterTask(Mockito.anyString())).thenReturn(Optional.empty());
        final PolarisProjectSyncTask polarisProjectSyncTask = new PolarisProjectSyncTask(null, null, null, null, null, null);
        final PolarisProvider polarisProvider = new PolarisProvider(taskManager, polarisProjectSyncTask, null, null);
        polarisProvider.destroy();
    }

    @Disabled
    @Test
    public void getProviderContentTypesTest() {
        // TODO implement
    }

    @Test
    public void getSupportedFormatTypes() {
        final PolarisProvider polarisProvider = new PolarisProvider(null, null, null, null);
        final Set<FormatType> formatTypes = polarisProvider.getSupportedFormatTypes();

        assertTrue(formatTypes.contains(FormatType.DEFAULT));
    }
}
