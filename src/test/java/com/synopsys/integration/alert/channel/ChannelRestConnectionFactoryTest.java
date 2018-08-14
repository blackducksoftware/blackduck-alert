package com.synopsys.integration.alert.channel;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.exception.EncryptionException;
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
    public void testConnectionFields() throws EncryptionException {
        final String host = "host";
        final int port = 1;
        final Credentials credentials = new Credentials("username", "password");

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertProxyHost(host);
        testAlertProperties.setAlertProxyUsername(credentials.getUsername());
        testAlertProperties.setAlertProxyPassword(credentials.getDecryptedPassword());
        testAlertProperties.setAlertProxyPort(String.valueOf(port));
        testAlertProperties.setAlertTrustCertificate(true);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);

        final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection("https:url");

        final ProxyInfo expectedProxyInfo = new ProxyInfo(host, port, credentials, null, null, null);

        assertNotNull(restConnection);
        assertEquals(expectedProxyInfo, restConnection.getProxyInfo());
    }

    @Test
    public void testNullUrl() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties);
        final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection("bad");

        assertNull(restConnection);
        assertTrue(outputLogger.isLineContainingText("Problem generating the URL: "));
    }
}
