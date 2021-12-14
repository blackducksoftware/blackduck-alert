package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.RestApiAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.database.api.DefaultConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.alert.task.PhoneHomeTask;
import com.synopsys.integration.alert.web.api.about.AboutReader;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class PhoneHomeTest {
    private static final String TEST_VERSION = "1.2.3";
    private static final String TEST_DESCRIPTOR_NAME = "Test Desc";

    @Test
    public void runTest() {
        RestApiAuditAccessor auditAccessor = Mockito.mock(RestApiAuditAccessor.class);
        Mockito.when(auditAccessor.findFirstByJobId(Mockito.any())).thenReturn(Optional.empty());
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);

        AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getProductVersion()).thenReturn(TEST_VERSION);

        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        // FIXME implement mocks

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(DefaultConfigurationModelConfigurationAccessor.class);
        ConfigurationModel config = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(config));

        DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Descriptor descriptor = Mockito.mock(Descriptor.class);
        DescriptorKey descriptorKey = Mockito.mock(DescriptorKey.class);
        Mockito.when(descriptorKey.getUniversalKey()).thenReturn(TEST_DESCRIPTOR_NAME);

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Collections.singletonMap(descriptorKey, descriptor));
        List<ProviderPhoneHomeHandler> providerHandlers = List.of();

        ProviderKey providerKey = new BlackDuckProviderKey();

        PhoneHomeTask phoneHomeTask = new PhoneHomeTask(taskScheduler, aboutReader, jobAccessor, configurationModelConfigurationAccessor, null, proxyManager, new Gson(), auditAccessor, providerHandlers, providerKey);

        try {
            phoneHomeTask.run();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

}
