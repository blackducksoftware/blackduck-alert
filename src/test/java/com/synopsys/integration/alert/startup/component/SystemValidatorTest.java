package com.synopsys.integration.alert.startup.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.component.settings.validator.SettingsSystemValidator;
import com.blackduck.integration.alert.component.users.UserSystemValidator;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageAccessor;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProvider;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.synopsys.integration.alert.test.common.OutputLogger;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.credentials.Credentials;
import com.blackduck.integration.rest.credentials.CredentialsBuilder;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.proxy.ProxyInfoBuilder;
import com.blackduck.integration.rest.response.Response;

class SystemValidatorTest {
    private static final String DEFAULT_CONFIG_NAME = "Default";
    private static final String LOCALHOST = "https://localhost:443";

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
    void testValidate() {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("Black Duck URL"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Black Duck API Token");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        SettingsSystemValidator settingsSystemValidator = Mockito.mock(SettingsSystemValidator.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        UserSystemValidator userSystemValidator = Mockito.mock(UserSystemValidator.class);
        SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);

        SystemMessageInitializer systemValidator = new SystemMessageInitializer(
            List.of(),
            settingsSystemValidator,
            configurationModelConfigurationAccessor,
            userSystemValidator,
            systemMessageAccessor
        );
        systemValidator.validate();
    }

    @Test
    void testValidateProviders() throws IOException {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("Black Duck URL"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Black Duck API Token");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        SettingsSystemValidator settingsSystemValidator = Mockito.mock(SettingsSystemValidator.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        UserSystemValidator userSystemValidator = Mockito.mock(UserSystemValidator.class);
        SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);

        SystemMessageInitializer systemValidator = new SystemMessageInitializer(
            List.of(),
            settingsSystemValidator,
            configurationModelConfigurationAccessor,
            userSystemValidator,
            systemMessageAccessor
        );
        systemValidator.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    void testValidateBlackDuckProviderNullURL() throws Exception {
        long configId = 1L;
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.empty());
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        Mockito.when(blackDuckProperties.getConfigName()).thenReturn(DEFAULT_CONFIG_NAME);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(configId);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);

        String missingUrlMessageType = BlackDuckSystemValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(defaultSystemMessageUtility);
        blackDuckSystemValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(
                String.format(BlackDuckSystemValidator.MISSING_BLACKDUCK_URL_ERROR_W_CONFIG_FORMAT, DEFAULT_CONFIG_NAME),
                SystemMessageSeverity.WARNING,
                missingUrlMessageType
            );
    }

    @Test
    void testValidateBlackDuckProviderLocalhostURL() throws Exception {
        long configId = 1L;
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of(LOCALHOST));
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        Mockito.when(blackDuckProperties.getConfigName()).thenReturn(DEFAULT_CONFIG_NAME);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(configId);

        BlackDuckServerConfig blackDuckServerConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfig(Mockito.any(IntLogger.class))).thenReturn(blackDuckServerConfig);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);

        String localhostMessageType = BlackDuckSystemValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(defaultSystemMessageUtility);
        BlackDuckSystemValidator spiedBlackDuckSystemValidator = spy(blackDuckSystemValidator);
        Mockito.doReturn(true).when(spiedBlackDuckSystemValidator).canConnect(Mockito.eq(blackDuckProperties), Mockito.any(BlackDuckApiTokenValidator.class));
        spiedBlackDuckSystemValidator.validate(blackDuckProperties);

        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(
                String.format(BlackDuckSystemValidator.BLACKDUCK_LOCALHOST_ERROR_FORMAT, DEFAULT_CONFIG_NAME),
                SystemMessageSeverity.WARNING,
                localhostMessageType
            );
    }

    @Test
    void testValidateHubInvalidProvider() throws Exception {
        long configId = 1L;
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of(LOCALHOST));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Test Api Key");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        Mockito.when(blackDuckProperties.getConfigName()).thenReturn(DEFAULT_CONFIG_NAME);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(configId);

        BlackDuckServerConfig serverConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(serverConfig.canConnect(Mockito.any(IntLogger.class))).thenReturn(false);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfig(Mockito.any(IntLogger.class))).thenReturn(serverConfig);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);

        String localhostMessageType = BlackDuckSystemValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        String connectivityMessageType = BlackDuckSystemValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);

        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(defaultSystemMessageUtility);
        BlackDuckSystemValidator spiedBlackDuckSystemValidator = spy(blackDuckSystemValidator);
        Mockito.doReturn(false).when(spiedBlackDuckSystemValidator).canConnect(Mockito.eq(blackDuckProperties), Mockito.any(BlackDuckApiTokenValidator.class));
        spiedBlackDuckSystemValidator.validate(blackDuckProperties);

        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(
                String.format(BlackDuckSystemValidator.BLACKDUCK_LOCALHOST_ERROR_FORMAT, DEFAULT_CONFIG_NAME),
                SystemMessageSeverity.WARNING,
                localhostMessageType
            );
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(
                String.format("Can not connect to the Black Duck server with the configuration '%s'.", DEFAULT_CONFIG_NAME),
                SystemMessageSeverity.WARNING,
                connectivityMessageType
            );
    }

    @Test
    void testValidateHubValidProvider() throws Exception {
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://alert.example.com:443/alert"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Test Api Key");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);

        BlackDuckServerConfig serverConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(serverConfig.canConnect(Mockito.any(IntLogger.class))).thenReturn(true);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfig(Mockito.any(IntLogger.class))).thenReturn(serverConfig);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);

        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(defaultSystemMessageUtility);
        BlackDuckSystemValidator spiedBlackDuckSystemValidator = spy(blackDuckSystemValidator);
        Mockito.doReturn(true).when(spiedBlackDuckSystemValidator).canConnect(Mockito.eq(blackDuckProperties), Mockito.any(BlackDuckApiTokenValidator.class));
        spiedBlackDuckSystemValidator.validate(blackDuckProperties);

        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }

    @Test
    void testValidateHubValidProviderWithProxy() throws Exception {
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

        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(expectedProxyInfo);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("Black Duck URL"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Black Duck API Token");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);

        BlackDuckServerConfig serverConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(serverConfig.canConnect(Mockito.any(IntLogger.class))).thenReturn(false);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);

        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(defaultSystemMessageUtility);
        blackDuckSystemValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }

    @Test
    void testCanConnectTrue() throws IOException, IntegrationException {
        BlackDuckProperties blackDuckProperties = createMockBlackDuckProperties(Optional.of(LOCALHOST));

        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(defaultSystemMessageUtility);

        Response canConnectResponse = Mockito.mock(Response.class);
        Mockito.when(canConnectResponse.isStatusCodeSuccess()).thenReturn(true);

        BlackDuckApiTokenValidator spiedBlackDuckApiTokenValidator = spy(new BlackDuckApiTokenValidator(blackDuckProperties));
        Mockito.doReturn(canConnectResponse).when(spiedBlackDuckApiTokenValidator).attemptAuthentication();

        boolean canConnectResult = blackDuckSystemValidator.canConnect(blackDuckProperties, spiedBlackDuckApiTokenValidator);

        assertTrue(canConnectResult);
        assertTrue(outputLogger.isLineContainingText("Attempting connection to " + LOCALHOST));
        assertTrue(outputLogger.isLineContainingText("Successfully connected to " + LOCALHOST));
    }

    @Test
    void testCanConnectFalse() throws IOException, IntegrationException {
        int httpResponseCode = 409;
        BlackDuckProperties blackDuckProperties = createMockBlackDuckProperties(Optional.of(LOCALHOST));

        DefaultSystemMessageAccessor mockDefaultSystemMessageAccessor = Mockito.mock(DefaultSystemMessageAccessor.class);
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(mockDefaultSystemMessageAccessor);

        Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.isStatusCodeSuccess()).thenReturn(false);
        Mockito.when(mockResponse.getStatusCode()).thenReturn(httpResponseCode);

        BlackDuckApiTokenValidator spiedBlackDuckApiTokenValidator = spy(new BlackDuckApiTokenValidator(blackDuckProperties));
        Mockito.doReturn(mockResponse).when(spiedBlackDuckApiTokenValidator).attemptAuthentication();

        boolean canConnectResult = blackDuckSystemValidator.canConnect(blackDuckProperties, spiedBlackDuckApiTokenValidator);

        assertFalse(canConnectResult);
        assertTrue(outputLogger.isLineContainingText(String.format("Failed to make connection to %s; http status code: %d", LOCALHOST, httpResponseCode)));
    }

    @Test
    void testCanConnectThrowException() throws IOException, IntegrationException {
        BlackDuckProperties blackDuckProperties = createMockBlackDuckProperties(Optional.of(LOCALHOST));

        DefaultSystemMessageAccessor mockDefaultSystemMessageAccessor = Mockito.mock(DefaultSystemMessageAccessor.class);
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(mockDefaultSystemMessageAccessor);

        BlackDuckApiTokenValidator spiedBlackDuckApiTokenValidator = spy(new BlackDuckApiTokenValidator(blackDuckProperties));
        Mockito.doThrow(IntegrationException.class).when(spiedBlackDuckApiTokenValidator).attemptAuthentication();

        boolean canConnectResult = blackDuckSystemValidator.canConnect(blackDuckProperties, spiedBlackDuckApiTokenValidator);

        assertFalse(canConnectResult);
        assertTrue(outputLogger.isLineContainingText(String.format("Failed to make connection to %s; cause: ", LOCALHOST)));
    }

    @Test
    void testCanConnectNullBDUrl() throws IOException, IntegrationException {
        BlackDuckProperties blackDuckProperties = createMockBlackDuckProperties(Optional.empty());

        DefaultSystemMessageAccessor mockDefaultSystemMessageAccessor = Mockito.mock(DefaultSystemMessageAccessor.class);
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(mockDefaultSystemMessageAccessor);

        BlackDuckApiTokenValidator spiedBlackDuckApiTokenValidator = spy(new BlackDuckApiTokenValidator(blackDuckProperties));
        Mockito.doThrow(IntegrationException.class).when(spiedBlackDuckApiTokenValidator).attemptAuthentication();

        boolean canConnectResult = blackDuckSystemValidator.canConnect(blackDuckProperties, spiedBlackDuckApiTokenValidator);

        assertFalse(canConnectResult);
        assertTrue(outputLogger.isLineContainingText("Black Duck URL not configured."));
    }

    private BlackDuckProperties createMockBlackDuckProperties(Optional<String> blackDuckUrl) {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(blackDuckUrl);
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        Mockito.when(blackDuckProperties.getConfigName()).thenReturn(DEFAULT_CONFIG_NAME);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(1L);

        return blackDuckProperties;
    }
}
