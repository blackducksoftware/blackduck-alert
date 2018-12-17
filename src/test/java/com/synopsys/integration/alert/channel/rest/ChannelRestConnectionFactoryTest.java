package com.synopsys.integration.alert.channel.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

public class ChannelRestConnectionFactoryTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testConnectionFields() {
        final String host = "host";
        final int port = 1;
        final CredentialsBuilder builder = Credentials.newBuilder();
        builder.setUsername("username");
        builder.setPassword("password");
        final Credentials credentials = builder.build();

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertProxyHost(host);
        testAlertProperties.setAlertProxyUsername(credentials.getUsername().get());
        testAlertProperties.setAlertProxyPassword(credentials.getPassword().get());
        testAlertProperties.setAlertProxyPort(String.valueOf(port));
        testAlertProperties.setAlertTrustCertificate(true);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);

        final RestConnection restConnection = channelRestConnectionFactory.createRestConnection();
        final ProxyInfoBuilder proxyBuilder = ProxyInfo.newBuilder();
        proxyBuilder.setHost(host);
        proxyBuilder.setPort(port);
        proxyBuilder.setCredentials(credentials);
        proxyBuilder.setNtlmDomain(null);
        proxyBuilder.setNtlmWorkstation(null);
        final ProxyInfo expectedProxyInfo = proxyBuilder.build();

        assertNotNull(restConnection);
        assertEquals(expectedProxyInfo, restConnection.getProxyInfo());
    }
}
