package com.blackducksoftware.integration.hub.alert.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GlobalPropertiesTest {

    @Test
    public void testGetSetMethods() {
        final GlobalProperties properties = new GlobalProperties(null);
        final String hubUrl = "hubUrl";
        final Boolean hubTrustCertificate = Boolean.TRUE;
        final String proxyHost = "proxyHost";
        final String proxyPort = "proxyPort";
        final String proxyUser = "proxyUser";
        final String proxyPassword = "proxyPassword";
        final Boolean alertSSLEnabled = Boolean.TRUE;
        final String serverPort = "2138";
        final String keyStoreFile = "keystoreFile";
        final String keyStorePass = "keystorePass";
        final String keyStoreType = "keystoreType";
        final String keyAlias = "keyAlias";
        final String trustStoreFile = "trustStoreFile";
        final String trustStorePass = "trustStorePass";
        final String trustStoreType = "trustStoreType";

        properties.setHubUrl(hubUrl);
        properties.setHubTrustCertificate(hubTrustCertificate);
        properties.setHubProxyHost(proxyHost);
        properties.setHubProxyPort(proxyPort);
        properties.setHubProxyUsername(proxyUser);
        properties.setHubProxyPassword(proxyPassword);
        properties.setAlertSSLEnabled(alertSSLEnabled);
        properties.setServerPort(serverPort);
        properties.setKeyStoreFile(keyStoreFile);
        properties.setKeyStorePass(keyStorePass);
        properties.setKeyStoreType(keyStoreType);
        properties.setKeyAlias(keyAlias);
        properties.setTrustStoreFile(trustStoreFile);
        properties.setTrustStorePass(trustStorePass);
        properties.setTrustStoreType(trustStoreType);

        assertEquals(hubTrustCertificate, properties.getHubTrustCertificate());
        assertEquals(proxyHost, properties.getHubProxyHost());
        assertEquals(proxyPort, properties.getHubProxyPort());
        assertEquals(proxyUser, properties.getHubProxyUsername());
        assertEquals(proxyPassword, properties.getHubProxyPassword());

        assertEquals(hubUrl, properties.getHubUrl());
        assertEquals(alertSSLEnabled, properties.getAlertSSLEnabled());
        assertEquals(serverPort, properties.getServerPort());
        assertEquals(keyStoreFile, properties.getKeyStoreFile());
        assertEquals(keyStorePass, properties.getKeyStorePass());
        assertEquals(keyStoreType, properties.getKeyStoreType());
        assertEquals(keyAlias, properties.getKeyAlias());
        assertEquals(trustStoreFile, properties.getTrustStoreFile());
        assertEquals(trustStorePass, properties.getTrustStorePass());
        assertEquals(trustStoreType, properties.getTrustStoreType());
    }
}
