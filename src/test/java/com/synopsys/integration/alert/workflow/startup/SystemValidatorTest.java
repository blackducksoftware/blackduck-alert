package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.database.api.UserAccessor;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

public class SystemValidatorTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (outputLogger != null) {
            outputLogger.cleanup();
        }
    }

    @Test
    public void testValidate() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck URL");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        final SystemValidator spiedSystemValidator = Mockito.spy(systemValidator);

        spiedSystemValidator.validate();
        Mockito.verify(systemStatusUtility).setSystemInitialized(Mockito.anyBoolean());
    }

    @Test
    public void testValidateEncryptionProperties() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        systemValidator.validateEncryptionProperties(new HashMap<>());
        Mockito.verify(encryptionUtility).isInitialized();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Not Initialized"));
    }

    @Test
    public void testValidateEncryptionPropertiesSuccess() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        systemValidator.validateEncryptionProperties(new HashMap<>());
        Mockito.verify(encryptionUtility).isInitialized();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Initialized"));
    }

    @Test
    public void testValidateProviders() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck URL");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        systemValidator.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testvalidateBlackDuckProviderNullURL() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        testGlobalProperties.setBlackDuckUrl(null);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Invalid; cause: Black Duck URL missing..."));
    }

    @Test
    public void testvalidateBlackDuckProviderLocalhostURL() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to"));
    }

    @Test
    public void testvalidateBlackDuckProviderHubWebserverEnvironmentSet() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final TestBlackDuckProperties spiedGlobalProperties = Mockito.spy(testGlobalProperties);
        spiedGlobalProperties.setBlackDuckUrl("https://localhost:443");
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, spiedGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to"));
    }

    @Test
    public void testValidateHubInvalidProvider() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Invalid; cause:"));
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testValidateHubValidProvider() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Valid!"));
    }

    @Test
    public void testValidateHubValidProviderWithProxy() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);

        final CredentialsBuilder builder = Credentials.newBuilder();
        builder.setUsername("AUser");
        builder.setPassword("aPassword");
        final Credentials credentials = builder.build();

        final ProxyInfoBuilder proxyBuilder = ProxyInfo.newBuilder();
        proxyBuilder.setHost("google.com");
        proxyBuilder.setPort(3218);
        proxyBuilder.setCredentials(credentials);
        proxyBuilder.setNtlmDomain(null);
        proxyBuilder.setNtlmWorkstation(null);
        final ProxyInfo expectedProxyInfo = proxyBuilder.build();

        Mockito.when(proxyManager.createProxyInfo()).thenReturn(expectedProxyInfo);
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck URL");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        testGlobalProperties.setBlackDuckTimeout(1);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility, userAccessor, proxyManager);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Invalid; cause:"));
    }
}
