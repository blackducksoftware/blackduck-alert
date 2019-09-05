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
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisContent;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;

public class PolarisProviderTest {
    private static final PolarisProviderKey POLARIS_PROVIDER_KEY = new PolarisProviderKey();

    @Test
    public void initializeTest() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final PolarisProjectSyncTask polarisProjectSyncTask = Mockito.mock(PolarisProjectSyncTask.class);
        final PolarisProperties polarisProperties = Mockito.mock(PolarisProperties.class);

        Mockito.when(taskManager.unregisterTask(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(polarisProjectSyncTask.getTaskName()).thenReturn("task");
        Mockito.doNothing().when(polarisProjectSyncTask).run();
        Mockito.when(polarisProperties.createPolarisHttpClientSafely((Logger) Mockito.any())).thenReturn(Optional.empty());

        final PolarisProvider polarisProvider = new PolarisProvider(POLARIS_PROVIDER_KEY, taskManager, polarisProjectSyncTask, polarisProperties, null, null);
        polarisProvider.initialize();
    }

    @Test
    public void destroy() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.unregisterTask(Mockito.anyString())).thenReturn(Optional.empty());
        final PolarisProjectSyncTask polarisProjectSyncTask = new PolarisProjectSyncTask(POLARIS_PROVIDER_KEY, null, null, null, null, null, null);
        final PolarisProvider polarisProvider = new PolarisProvider(POLARIS_PROVIDER_KEY, taskManager, polarisProjectSyncTask, null, null, null);
        polarisProvider.destroy();
    }

    @Disabled
    @Test
    public void getProviderContentTypesTest() {
        // TODO implement
    }

    @Test
    public void getSupportedFormatTypes() {
        final PolarisContent polarisContent = new PolarisContent(POLARIS_PROVIDER_KEY);
        final PolarisProvider polarisProvider = new PolarisProvider(POLARIS_PROVIDER_KEY, null, null, null, polarisContent, null);
        final Set<FormatType> formatTypes = polarisProvider.getProviderContent().getSupportedContentFormats();

        assertTrue(formatTypes.contains(FormatType.DEFAULT));
    }

}
