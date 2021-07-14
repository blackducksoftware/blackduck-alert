package com.synopsys.integration.alert.provider.blackduck.saml;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class BlackDuckSSOConfigRetrieverTest {
    @Test
    public void retrieveExceptionTest() throws IntegrationException {
        HttpUrl baseUrl = new HttpUrl("https://a-blackduck-server");
        ApiDiscovery apiDiscovery = new ApiDiscovery(baseUrl);

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(
            blackDuckApiClient.getResponse(Mockito.any(BlackDuckRequest.class))
        ).thenThrow(new AlertException());

        BlackDuckSSOConfigRetriever ssoConfigRetriever = new BlackDuckSSOConfigRetriever(apiDiscovery, blackDuckApiClient);
        try {
            ssoConfigRetriever.retrieve();
            fail(String.format("Expected an %s to be thrown", AlertException.class.getSimpleName()));
        } catch (AlertException e) {
            // Pass
        }
    }

    @Test
    @Tags({
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_BLACKDUCK_CONNECTION)
    })
    public void retrieveTestIT() throws AlertException {
        BlackDuckProperties blackDuckProperties = createBlackDuckProperties();
        BlackDuckSSOConfigRetriever ssoConfigRetriever = BlackDuckSSOConfigRetriever.fromProperties(blackDuckProperties);
        try {
            BlackDuckSSOConfigView ssoConfig = ssoConfigRetriever.retrieve();
            System.out.println("SSO Config:");
            System.out.println(ssoConfig);
        } catch (AlertException e) {
            fail("SSO Config Retrieval Failed", e);
        }
    }

    private BlackDuckProperties createBlackDuckProperties() {
        TestProperties testProperties = new TestProperties();
        String blackDuckUrl = testProperties.getBlackDuckURL();
        String blackDuckApiKey = testProperties.getBlackDuckAPIToken();
        String blackDuckTimeout = testProperties.getOptionalProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT).orElse("300");

        MockAlertProperties mockAlertProperties = new MockAlertProperties();
        ProxyManager mockProxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(mockProxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);

        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(0L, 0L, null, null, ConfigContextEnum.GLOBAL);
        configurationModel.put(createConfigFieldModel(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED, Boolean.TRUE.toString()));
        configurationModel.put(createConfigFieldModel(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, BlackDuckSSOConfigRetrieverTest.class.getSimpleName()));
        configurationModel.put(createConfigFieldModel(BlackDuckDescriptor.KEY_BLACKDUCK_URL, blackDuckUrl));
        configurationModel.put(createConfigFieldModel(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, blackDuckApiKey));
        configurationModel.put(createConfigFieldModel(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, blackDuckTimeout));

        return new BlackDuckProperties(0L, new Gson(), BlackDuckServicesFactory.createDefaultObjectMapper(), mockAlertProperties, mockProxyManager, configurationModel);
    }

    private ConfigurationFieldModel createConfigFieldModel(String key, String value) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValue(value);
        return configurationFieldModel;
    }

}
