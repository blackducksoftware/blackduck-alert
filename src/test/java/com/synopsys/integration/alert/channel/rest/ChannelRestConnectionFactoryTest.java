package com.synopsys.integration.alert.channel.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.ProxyManager;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

public class ChannelRestConnectionFactoryTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testConnectionFields() throws Exception {
        final String host = "host";
        final int port = 1;
        final CredentialsBuilder builder = Credentials.newBuilder();
        builder.setUsername("username");
        builder.setPassword("password");
        final Credentials credentials = builder.build();

        final ProxyInfoBuilder proxyBuilder = ProxyInfo.newBuilder();
        proxyBuilder.setHost(host);
        proxyBuilder.setPort(port);
        proxyBuilder.setCredentials(credentials);
        proxyBuilder.setNtlmDomain(null);
        proxyBuilder.setNtlmWorkstation(null);
        final ProxyInfo expectedProxyInfo = proxyBuilder.build();

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertTrustCertificate(true);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.getAlertProxyHost()).thenReturn(Optional.of(host));
        Mockito.when(proxyManager.getAlertProxyUsername()).thenReturn(Optional.of(credentials.getUsername().get()));
        Mockito.when(proxyManager.getAlertProxyPassword()).thenReturn(Optional.of(credentials.getPassword().get()));
        Mockito.when(proxyManager.getAlertProxyHost()).thenReturn(Optional.of(String.valueOf(port)));
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(expectedProxyInfo);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);

        final RestConnection restConnection = channelRestConnectionFactory.createRestConnection();

        assertNotNull(restConnection);
        assertEquals(expectedProxyInfo, restConnection.getProxyInfo());
    }
}
