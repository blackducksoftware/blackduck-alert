package com.synopsys.integration.alert.provider.blackduck.descriptor;

public class BlackDuckDistributionDescriptorActionApiTest {

    private final BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig();

    //    @Test
    //    public void testValidateConfig() {
    //        final BlackDuckDistributionUIConfig uiConfig = Mockito.mock(BlackDuckDistributionUIConfig.class);
    //        Mockito.when(uiConfig.createFields()).thenReturn(blackDuckDistributionUIConfig.createFields());
    //        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi(Mockito.mock(ContentConverter.class));
    //        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
    //        final FieldModel fieldAccessor = Mockito.mock(FieldModel.class);
    //        Mockito.when(fieldAccessor.getField(Mockito.anyString())).thenReturn(Optional.empty());
    //        spiedRestApi.validateConfig(uiConfig.createFields(), fieldAccessor, new HashMap<>());
    //        Mockito.verify(spiedRestApi).validateConfig(Mockito.anyCollection(), Mockito.any(), Mockito.anyMap());
    //    }
    //
    //    @Test
    //    public void testTestConfig() throws IntegrationException {
    //        final BlackDuckDistributionUIConfig uiConfig = Mockito.mock(BlackDuckDistributionUIConfig.class);
    //        Mockito.when(uiConfig.createFields()).thenReturn(blackDuckDistributionUIConfig.createFields());
    //        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi(Mockito.mock(ContentConverter.class));
    //        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
    //        spiedRestApi.testConfig(Mockito.anyCollection(), Mockito.any());
    //        Mockito.verify(spiedRestApi).testConfig(Mockito.anyCollection(), Mockito.any());
    //    }
}
