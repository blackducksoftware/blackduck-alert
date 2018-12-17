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
import com.synopsys.integration.rest.proxy.ProxyInfo;

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
        final Credentials credentials = new Credentials("username", "password");

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertProxyHost(host);
        testAlertProperties.setAlertProxyUsername(credentials.getUsername());
        testAlertProperties.setAlertProxyPassword(credentials.getPassword());
        testAlertProperties.setAlertProxyPort(String.valueOf(port));
        testAlertProperties.setAlertTrustCertificate(true);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);

        final RestConnection restConnection = channelRestConnectionFactory.createRestConnection();

        final ProxyInfo expectedProxyInfo = new ProxyInfo(host, port, credentials, null, null);

        assertNotNull(restConnection);
        assertEquals(expectedProxyInfo, restConnection.getProxyInfo());
    }
}
