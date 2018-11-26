package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class SystemSetupModelTest {

    @Test
    public void testDefaultConstructor() {
        final SystemSetupModel configuration = new SystemSetupModel();

        assertNull(configuration.getBlackDuckProviderUrl());
        assertNull(configuration.getBlackDuckConnectionTimeout());
        assertNull(configuration.getBlackDuckApiToken());
        assertFalse(configuration.isBlackDuckApiTokenSet());
        assertNull(configuration.getGlobalEncryptionPassword());
        assertFalse(configuration.isGlobalEncryptionPasswordSet());
        assertNull(configuration.getGlobalEncryptionSalt());
        assertFalse(configuration.isGlobalEncryptionSaltSet());
        assertNull(configuration.getProxyHost());
        assertNull(configuration.getProxyPort());
        assertNull(configuration.getProxyUsername());
        assertNull(configuration.getProxyPassword());
        assertFalse(configuration.isProxyPasswordSet());
    }

    @Test
    public void testFullConstructor() {
        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final boolean blackDuckApiTokenSet = true;
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = true;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";
        final boolean proxyPasswordSet = true;

        final SystemSetupModel configuration = SystemSetupModel.of(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken, blackDuckApiTokenSet,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword, proxyPasswordSet);

        assertEquals(blackDuckProviderUrl, configuration.getBlackDuckProviderUrl());
        assertEquals(blackDuckConnectionTimeout, configuration.getBlackDuckConnectionTimeout());
        assertEquals(blackDuckApiToken, configuration.getBlackDuckApiToken());
        assertTrue(configuration.isBlackDuckApiTokenSet());
        assertEquals(globalEncryptionPassword, configuration.getGlobalEncryptionPassword());
        assertTrue(configuration.isGlobalEncryptionPasswordSet());
        assertEquals(globalEncryptionSalt, configuration.getGlobalEncryptionSalt());
        assertTrue(configuration.isGlobalEncryptionSaltSet());
        assertEquals(proxyHost, configuration.getProxyHost());
        assertEquals(proxyPort, configuration.getProxyPort());
        assertEquals(proxyUsername, configuration.getProxyUsername());
        assertEquals(proxyPassword, configuration.getProxyPassword());
        assertTrue(configuration.isProxyPasswordSet());
    }
}
