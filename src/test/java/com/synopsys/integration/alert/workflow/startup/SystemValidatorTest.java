package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.component.settings.SettingsValidator;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckValidator;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.workflow.startup.component.SystemMessageInitializer;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

public class SystemValidatorTest {
    private Logger logger = LoggerFactory.getLogger(SystemValidatorTest.class);
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
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck URL");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);

        SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), settingsValidator);
        systemValidator.validate();
    }

    @Test
    public void testValidateProviders() throws IOException {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck URL");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), settingsValidator);
        systemValidator.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testvalidateBlackDuckProviderNullURL() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, defaultSystemMessageUtility);
        testGlobalProperties.setBlackDuckUrl(null);
        blackDuckValidator.validate(testGlobalProperties);
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider invalid: URL missing"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING));
    }

    @Test
    public void testvalidateBlackDuckProviderLocalhostURL() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, new TestProperties(), proxyManager);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, defaultSystemMessageUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        blackDuckValidator.validate(testGlobalProperties);
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider Using localhost"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST));
    }

    @Test
    public void testValidateBlackDuckProviderHubWebserverEnvironmentSet() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, new TestProperties(), proxyManager);

        TestBlackDuckProperties spiedGlobalProperties = Mockito.spy(testGlobalProperties);
        spiedGlobalProperties.setBlackDuckUrl("https://localhost:443/alert");
        spiedGlobalProperties.setBlackDuckApiKey("Test Api Key");
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, defaultSystemMessageUtility);
        blackDuckValidator.validate(spiedGlobalProperties);
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider Using localhost"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST));
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq("Can not connect to the Black Duck server with the current configuration."), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY));
    }

    @Test
    public void testValidateHubInvalidProvider() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, new TestProperties(), proxyManager);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, defaultSystemMessageUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443/alert");
        testGlobalProperties.setBlackDuckApiKey("Test Api Key");
        blackDuckValidator.validate(testGlobalProperties);
        Mockito.verify(defaultSystemMessageUtility).addSystemMessage(Mockito.eq("Black Duck Provider Using localhost"), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST));
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq("Can not connect to the Black Duck server with the current configuration."), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY));
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testValidateHubValidProvider() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(new Gson(), testAlertProperties, new TestProperties(), proxyManager);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, defaultSystemMessageUtility);
        blackDuckValidator.validate(testGlobalProperties);
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }

    @Test
    public void testValidateHubValidProviderWithProxy() {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);

        CredentialsBuilder builder = Credentials.newBuilder();
        builder.setUsername("AUser");
        builder.setPassword("aPassword");
        Credentials credentials = builder.build();

        ProxyInfoBuilder proxyBuilder = ProxyInfo.newBuilder();
        proxyBuilder.setHost("google.com");
        proxyBuilder.setPort(3218);
        proxyBuilder.setCredentials(credentials);
        proxyBuilder.setNtlmDomain(null);
        proxyBuilder.setNtlmWorkstation(null);
        ProxyInfo expectedProxyInfo = proxyBuilder.build();

        Mockito.when(proxyManager.createProxyInfo()).thenReturn(expectedProxyInfo);
        TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck URL");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        testGlobalProperties.setBlackDuckTimeout(1);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(testAlertProperties, defaultSystemMessageUtility);
        blackDuckValidator.validate(testGlobalProperties);
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }
}
