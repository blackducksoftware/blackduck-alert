package com.blackducksoftware.integration.alert.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.alert.OutputLogger;
import com.blackducksoftware.integration.alert.TestGlobalProperties;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;

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

        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        globalProperties.setHubProxyHost(host);
        globalProperties.setHubProxyUsername(credentials.getUsername());
        globalProperties.setHubProxyPassword(credentials.getDecryptedPassword());
        globalProperties.setHubProxyPort(String.valueOf(port));
        globalProperties.setHubTrustCertificate(true);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(globalProperties);

        final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection("https:url");

        final ProxyInfo expectedProxyInfo = new ProxyInfo(host, port, credentials, null, null, null);

        assertNotNull(restConnection);
        assertEquals(expectedProxyInfo, restConnection.getProxyInfo());
    }

    @Test
    public void testNullUrl() throws IOException {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(globalProperties);
        final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection("bad");

        assertNull(restConnection);
        assertTrue(outputLogger.isLineContainingText("Problem generating the URL: "));
    }
}
