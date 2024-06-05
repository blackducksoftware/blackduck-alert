package com.synopsys.integration.alert.api.channel.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.certificates.AlertSSLContextManager;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

class ChannelRestConnectionFactoryTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Test
    void testConnectionFields() {
        String baseUrl = "https://example-base-url";
        final String host = "host";
        final int port = 1;
        CredentialsBuilder builder = Credentials.newBuilder();
        builder.setUsername("username");
        builder.setPassword("password");
        Credentials credentials = builder.build();

        ProxyInfoBuilder proxyBuilder = ProxyInfo.newBuilder();
        proxyBuilder.setHost(host);
        proxyBuilder.setPort(port);
        proxyBuilder.setCredentials(credentials);
        proxyBuilder.setNtlmDomain(null);
        proxyBuilder.setNtlmWorkstation(null);
        ProxyInfo expectedProxyInfo = proxyBuilder.build();

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        testAlertProperties.setAlertTrustCertificate(true);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        AlertSSLContextManager alertSSLContextManager = Mockito.mock(AlertSSLContextManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(expectedProxyInfo);
        Mockito.when(alertSSLContextManager.buildWithClientCertificate()).thenReturn(Optional.empty());
        ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager, gson, alertSSLContextManager);

        IntHttpClient intHttpClient = channelRestConnectionFactory.createIntHttpClient(baseUrl);

        assertNotNull(intHttpClient);
        assertEquals(expectedProxyInfo, intHttpClient.getProxyInfo());
    }

}
