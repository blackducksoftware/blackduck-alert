package com.synopsys.integration.alert.workflow.startup.install;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class RequiredSystemConfigurationTest {

    @Test
    public void testEncryptionAlreadySet() {
        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final boolean isGlobalEncryptionPasswordSet = true;
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final RequiredSystemConfiguration configuration = new RequiredSystemConfiguration(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken,
            isGlobalEncryptionPasswordSet, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword);

        assertEquals(blackDuckProviderUrl, configuration.getBlackDuckProviderUrl());
        assertEquals(blackDuckConnectionTimeout, configuration.getBlackDuckConnectionTimeout());
        assertEquals(blackDuckApiToken, configuration.getBlackDuckApiToken());
        assertNull(configuration.getGlobalEncryptionPassword());
        assertTrue(configuration.isGlobalEncryptionPasswordSet());
        assertNull(configuration.getGlobalEncryptionSalt());
        assertTrue(configuration.isGloblaEncryptionSaltSet());
        assertEquals(proxyHost, configuration.getProxyHost());
        assertEquals(proxyPort, configuration.getProxyPort());
        assertEquals(proxyUsername, configuration.getProxyUsername());
        assertEquals(proxyPassword, configuration.getProxyPassword());
    }

    @Test
    public void testFullConstructor() {
        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = true;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final RequiredSystemConfiguration configuration = new RequiredSystemConfiguration(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword);

        assertEquals(blackDuckProviderUrl, configuration.getBlackDuckProviderUrl());
        assertEquals(blackDuckConnectionTimeout, configuration.getBlackDuckConnectionTimeout());
        assertEquals(blackDuckApiToken, configuration.getBlackDuckApiToken());
        assertEquals(globalEncryptionPassword, configuration.getGlobalEncryptionPassword());
        assertTrue(configuration.isGlobalEncryptionPasswordSet());
        assertEquals(globalEncryptionSalt, configuration.getGlobalEncryptionSalt());
        assertTrue(configuration.isGloblaEncryptionSaltSet());
        assertEquals(proxyHost, configuration.getProxyHost());
        assertEquals(proxyPort, configuration.getProxyPort());
        assertEquals(proxyUsername, configuration.getProxyUsername());
        assertEquals(proxyPassword, configuration.getProxyPassword());
    }
}
