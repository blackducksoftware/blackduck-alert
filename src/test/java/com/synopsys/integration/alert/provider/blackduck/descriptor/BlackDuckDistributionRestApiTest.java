package com.synopsys.integration.alert.provider.blackduck.descriptor;

import org.junit.Test;
import org.mockito.Mockito;

public class BlackDuckDistributionRestApiTest {

    @Test
    public void testValidateConfig() {
        final BlackDuckDistributionRestApi restApi = new BlackDuckDistributionRestApi(Mockito.mock(BlackDuckTypeConverter.class), Mockito.mock(BlackDuckRepositoryAccessor.class));
        final BlackDuckDistributionRestApi spiedRestApi = Mockito.spy(restApi);
        spiedRestApi.validateConfig(Mockito.any(), Mockito.anyMap());
        Mockito.verify(spiedRestApi).validateConfig(Mockito.any(), Mockito.anyMap());
    }

    @Test
    public void testTestConfig() {
        final BlackDuckDistributionRestApi restApi = new BlackDuckDistributionRestApi(Mockito.mock(BlackDuckTypeConverter.class), Mockito.mock(BlackDuckRepositoryAccessor.class));
        final BlackDuckDistributionRestApi spiedRestApi = Mockito.spy(restApi);
        spiedRestApi.testConfig(Mockito.any());
        Mockito.verify(spiedRestApi).testConfig(Mockito.any());
    }
}
