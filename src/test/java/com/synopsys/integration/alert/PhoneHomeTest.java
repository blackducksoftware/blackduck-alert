package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeHandlerMap;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class PhoneHomeTest {
    private static final String TEST_VERSION = "1.2.3";
    private static final String TEST_DESCRIPTOR_NAME = "Test Desc";

    @Test
    public void runTest() throws AlertDatabaseConstraintException {
        PhoneHomeHandlerMap phoneHomeHandlerMap = Mockito.mock(PhoneHomeHandlerMap.class);
        AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        Mockito.when(auditUtility.findFirstByJobId(Mockito.any())).thenReturn(Optional.empty());
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        TestBlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(new Gson(), new TestAlertProperties(), new TestProperties(), proxyManager);

        AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn(TEST_VERSION);

        DefaultConfigurationAccessor configurationAccessor = Mockito.mock(DefaultConfigurationAccessor.class);
        ConfigurationModel config = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(config));

        DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Descriptor descriptor = Mockito.mock(Descriptor.class);
        DescriptorKey descriptorKey = Mockito.mock(DescriptorKey.class);
        Mockito.when(descriptorKey.getUniversalKey()).thenReturn(TEST_DESCRIPTOR_NAME);

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Collections.singletonMap(descriptorKey, descriptor));
        List<Provider> providers = List.of();
        PhoneHomeTask phoneHomeTask = new PhoneHomeTask(taskScheduler, aboutReader, configurationAccessor, null, proxyManager, new Gson(), auditUtility, providers, phoneHomeHandlerMap);

        try {
            phoneHomeTask.run();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
}
