package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckValidator;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.workflow.startup.component.SystemMessageInitializer;
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
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);

        final SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), encryptionUtility, defaultSystemMessageUtility, userAccessor);
        systemValidator.validate();
    }

    @Test
    public void testValidateEncryptionProperties() throws IOException {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), encryptionUtility, defaultSystemMessageUtility, userAccessor);
        systemValidator.validateEncryptionProperties(new HashMap<>());
        Mockito.verify(encryptionUtility).isInitialized();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Not Initialized"));
    }

    @Test
    public void testValidateEncryptionPropertiesSuccess() throws IOException {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), encryptionUtility, defaultSystemMessageUtility, userAccessor);
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
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), encryptionUtility, defaultSystemMessageUtility, userAccessor);
        systemValidator.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testvalidateBlackDuckProviderNullURL() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, testGlobalProperties, defaultSystemMessageUtility);
        testGlobalProperties.setBlackDuckUrl(null);
        blackDuckValidator.validate();
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider invalid: URL missing"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING));
    }

    @Test
    public void testvalidateBlackDuckProviderLocalhostURL() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, Mockito.mock(ConfigurationAccessor.class), proxyManager);
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, testGlobalProperties, defaultSystemMessageUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        blackDuckValidator.validate();
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider Using localhost"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST));
    }

    @Test
    public void testValidateBlackDuckProviderHubWebserverEnvironmentSet() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, Mockito.mock(ConfigurationAccessor.class), proxyManager);

        final TestBlackDuckProperties spiedGlobalProperties = Mockito.spy(testGlobalProperties);
        spiedGlobalProperties.setBlackDuckUrl("https://localhost:443/alert");
        spiedGlobalProperties.setBlackDuckApiKey("Test Api Key");
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, spiedGlobalProperties, defaultSystemMessageUtility);
        blackDuckValidator.validate();
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider Using localhost"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST));
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq("Can not connect to the Black Duck server with the current configuration."), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY));
    }

    @Test
    public void testValidateHubInvalidProvider() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, Mockito.mock(ConfigurationAccessor.class), proxyManager);
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, testGlobalProperties, defaultSystemMessageUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443/alert");
        testGlobalProperties.setBlackDuckApiKey("Test Api Key");
        blackDuckValidator.validate();
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider Using localhost"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST));
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq("Can not connect to the Black Duck server with the current configuration."), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY));
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testValidateHubValidProvider() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, Mockito.mock(ConfigurationAccessor.class), proxyManager);
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, testGlobalProperties, defaultSystemMessageUtility);
        blackDuckValidator.validate();
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }

    @Test
    public void testValidateHubValidProviderWithProxy() {
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
        final DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        final BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, testGlobalProperties, defaultSystemMessageUtility);
        blackDuckValidator.validate();
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }
}
