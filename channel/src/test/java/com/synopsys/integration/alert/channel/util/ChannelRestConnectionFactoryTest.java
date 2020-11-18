package com.synopsys.integration.alert.channel.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.test.common.TestAlertProperties;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

public class ChannelRestConnectionFactoryTest {
    @Test
    public void testConnectionFields() {
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

        TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertTrustCertificate(true);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(expectedProxyInfo);
        ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);

        IntHttpClient intHttpClient = channelRestConnectionFactory.createIntHttpClient();

        assertNotNull(intHttpClient);
        assertEquals(expectedProxyInfo, intHttpClient.getProxyInfo());
    }

}
