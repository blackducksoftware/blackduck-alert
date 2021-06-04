package com.synopsys.integration.alert.provider.blackduck.saml;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckSSOConfigRetrieverTest {
    @Test
    public void retrieveExceptionTest() throws IntegrationException {
        HttpUrl baseUrl = new HttpUrl("https://a-blackduck-server");
        ApiDiscovery apiDiscovery = new ApiDiscovery(baseUrl);

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckApiClient.getResponse((UrlSingleResponse<?>) Mockito.any(UrlSingleResponse.class))).thenThrow(new AlertException());

        BlackDuckSSOConfigRetriever ssoConfigRetriever = new BlackDuckSSOConfigRetriever(apiDiscovery, blackDuckApiClient);
        try {
            ssoConfigRetriever.retrieve();
            fail(String.format("Expected an %s to be thrown", AlertException.class.getSimpleName()));
        } catch (AlertException e) {
            // Pass
        }
    }

}
