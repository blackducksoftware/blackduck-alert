package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;

public class PhoneHomeTest {
    private static final String TEST_VERSION = "1.2.3";
    private static final String TEST_DESCRIPTOR_NAME = "Test Desc";

    @Test
    public void runTest() throws AlertDatabaseConstraintException {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final TestBlackDuckProperties bdProperties = new TestBlackDuckProperties(new TestAlertProperties());

        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn(TEST_VERSION);

        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel config = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(TEST_DESCRIPTOR_NAME)).thenReturn(Arrays.asList(config));

        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        final Descriptor descriptor = Mockito.mock(Descriptor.class);
        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Collections.singletonMap(TEST_DESCRIPTOR_NAME, descriptor));

        final PhoneHomeTask phoneHomeTask = new PhoneHomeTask(taskScheduler, bdProperties, aboutReader, configurationAccessor, descriptorMap);

        try {
            phoneHomeTask.run();
        } catch (final Exception e) {
            fail("Unexpected exception");
        }
    }
}
