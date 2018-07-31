package com.blackducksoftware.integration.alert.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.google.gson.Gson;

public class GlobalPropertiesTest {

    @Test
    public void testGetSetMethods() {
        final BlackDuckProperties properties = new BlackDuckProperties(null, new Gson());
        final String hubUrl = "hubUrl";
        final Boolean hubTrustCertificate = Boolean.TRUE;
        final String proxyHost = "proxyHost";
        final String proxyPort = "proxyPort";
        final String proxyUser = "proxyUser";
        final String proxyPassword = "proxyPassword";
        final String serverPort = "2138";
        final String keyStoreFile = "keystoreFile";
        final String keyStorePass = "keystorePass";
        final String keyStoreType = "keystoreType";
        final String keyAlias = "keyAlias";
        final String trustStoreFile = "trustStoreFile";
        final String trustStorePass = "trustStorePass";
        final String trustStoreType = "trustStoreType";

        properties.setBlackDuckUrl(hubUrl);
        properties.setBlackDuckTrustCertificate(hubTrustCertificate);
        properties.setBlackDuckProxyHost(proxyHost);
        properties.setBlackDuckProxyPort(proxyPort);
        properties.setBlackDuckProxyUsername(proxyUser);
        properties.setBlackDuckProxyPassword(proxyPassword);
        properties.setServerPort(serverPort);
        properties.setKeyStoreFile(keyStoreFile);
        properties.setKeyStorePass(keyStorePass);
        properties.setKeyStoreType(keyStoreType);
        properties.setKeyAlias(keyAlias);
        properties.setTrustStoreFile(trustStoreFile);
        properties.setTrustStorePass(trustStorePass);
        properties.setTrustStoreType(trustStoreType);

        assertEquals(hubTrustCertificate, properties.getBlackDuckTrustCertificate().get());
        assertEquals(proxyHost, properties.getBlackDuckProxyHost().get());
        assertEquals(proxyPort, properties.getBlackDuckProxyPort().get());
        assertEquals(proxyUser, properties.getBlackDuckProxyUsername().get());
        assertEquals(proxyPassword, properties.getBlackDuckProxyPassword().get());

        assertEquals(hubUrl, properties.getBlackDuckUrl().get());
        assertEquals(serverPort, properties.getServerPort());
        assertEquals(keyStoreFile, properties.getKeyStoreFile());
        assertEquals(keyStorePass, properties.getKeyStorePass());
        assertEquals(keyStoreType, properties.getKeyStoreType());
        assertEquals(keyAlias, properties.getKeyAlias());
        assertEquals(trustStoreFile, properties.getTrustStoreFile());
        assertEquals(trustStorePass, properties.getTrustStorePass());
        assertEquals(trustStoreType, properties.getTrustStoreType());
        assertNotEquals(BlackDuckProperties.PRODUCT_VERSION_UNKNOWN, properties.getProductVersion());
    }

    @Test
    public void testAboutReadException() {
        try {
            new BlackDuckProperties(null, null);
            fail();
        } catch (final RuntimeException ex) {

        }
    }

    @Test
    public void testGetVersionReturnUnknown() {
        final BlackDuckProperties hubProperties = new BlackDuckProperties(null, new Gson());
        try {
            hubProperties.readAboutInformation(null);
        } catch (final RuntimeException ex) {
            ex.printStackTrace();
        }
        assertEquals(BlackDuckProperties.PRODUCT_VERSION_UNKNOWN, hubProperties.getProductVersion());
    }
}
