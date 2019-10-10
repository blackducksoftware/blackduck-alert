package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class PhoneHomeTest {
    private static final String TEST_VERSION = "1.2.3";
    private static final String TEST_DESCRIPTOR_NAME = "Test Desc";

    @Test
    public void runTest() throws AlertDatabaseConstraintException {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        Mockito.when(auditUtility.findFirstByJobId(Mockito.any())).thenReturn(Optional.empty());
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final TestBlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(new Gson(), new TestAlertProperties(), Mockito.mock(ConfigurationAccessor.class), proxyManager);

        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn(TEST_VERSION);

        final DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        final ConfigurationModel config = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorName(TEST_DESCRIPTOR_NAME)).thenReturn(Arrays.asList(config));

        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        final Descriptor descriptor = Mockito.mock(Descriptor.class);
        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Collections.singletonMap(TEST_DESCRIPTOR_NAME, descriptor));

        final PhoneHomeTask phoneHomeTask = new PhoneHomeTask(taskScheduler, aboutReader, configurationAccessor, null, proxyManager, new Gson(), auditUtility, blackDuckProperties);

        try {
            phoneHomeTask.run();
        } catch (final Exception e) {
            fail("Unexpected exception");
        }
    }
}
