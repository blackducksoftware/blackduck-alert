package com.synopsys.integration.alert.provider.blackduck.descriptor;

import org.junit.Test;
import org.mockito.Mockito;

public class BlackDuckDistributionDescriptorActionApiTest {

    @Test
    public void testValidateConfig() {
        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi(Mockito.mock(BlackDuckTypeConverter.class), Mockito.mock(BlackDuckRepositoryAccessor.class));
        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
        spiedRestApi.validateConfig(Mockito.any(), Mockito.anyMap());
        Mockito.verify(spiedRestApi).validateConfig(Mockito.any(), Mockito.anyMap());
    }

    @Test
    public void testTestConfig() {
        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi(Mockito.mock(BlackDuckTypeConverter.class), Mockito.mock(BlackDuckRepositoryAccessor.class));
        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
        spiedRestApi.testConfig(Mockito.any(), Mockito.anyString());
        Mockito.verify(spiedRestApi).testConfig(Mockito.any(), Mockito.nullable(String.class));
    }
}
