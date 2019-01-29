package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckDistributionDescriptorActionApiTest {
    private final BlackDuckProvider provider = new BlackDuckProvider(null, null, null);
    private final BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig(provider);

    @Test
    public void testValidateConfig() {
        final BlackDuckDistributionUIConfig uiConfig = Mockito.mock(BlackDuckDistributionUIConfig.class);
        Mockito.when(uiConfig.createFields()).thenReturn(blackDuckDistributionUIConfig.createFields());
        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi();
        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
        final FieldModel fieldAccessor = Mockito.mock(FieldModel.class);
        Mockito.when(fieldAccessor.getField(Mockito.anyString())).thenReturn(Optional.empty());
        final Map<String, ConfigField> configFieldMap = uiConfig.createFields().stream().collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        spiedRestApi.validateConfig(configFieldMap, fieldAccessor, new HashMap<>());
        Mockito.verify(spiedRestApi).validateConfig(Mockito.anyMap(), Mockito.any(), Mockito.anyMap());
    }

    @Test
    public void testTestConfig() throws IntegrationException {
        final BlackDuckDistributionUIConfig uiConfig = Mockito.mock(BlackDuckDistributionUIConfig.class);
        Mockito.when(uiConfig.createFields()).thenReturn(blackDuckDistributionUIConfig.createFields());
        final BlackDuckDistributionDescriptorActionApi restApi = new BlackDuckDistributionDescriptorActionApi();
        final BlackDuckDistributionDescriptorActionApi spiedRestApi = Mockito.spy(restApi);
        spiedRestApi.testConfig(Mockito.any());
        Mockito.verify(spiedRestApi).testConfig(Mockito.any());
    }
}
