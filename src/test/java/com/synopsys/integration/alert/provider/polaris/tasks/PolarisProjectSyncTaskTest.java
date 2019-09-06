package com.synopsys.integration.alert.provider.polaris.tasks;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProviderKey;

public class PolarisProjectSyncTaskTest {
    @Test
    public void runPolarisPropertiesNotConfiguredTest() {
        final PolarisProperties polarisProperties = Mockito.mock(PolarisProperties.class);
        Mockito.when(polarisProperties.getUrl()).thenReturn(Optional.empty());

        final PolarisProjectSyncTask polarisProjectSyncTask = new PolarisProjectSyncTask(new PolarisProviderKey(), null, polarisProperties, null, null, null, null);
        polarisProjectSyncTask.run();
    }

}
