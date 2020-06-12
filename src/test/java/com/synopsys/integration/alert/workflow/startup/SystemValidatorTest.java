package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.component.settings.SettingsValidator;
import com.synopsys.integration.alert.component.users.UserSystemValidator;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckValidator;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.workflow.startup.component.SystemMessageInitializer;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

public class SystemValidatorTest {
    private static final String DEFAULT_CONFIG_NAME = "Default";
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
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("Black Duck URL"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Black Duck API Token");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        UserSystemValidator userSystemValidator = Mockito.mock(UserSystemValidator.class);
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);

        SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), settingsValidator, configurationAccessor, userSystemValidator, systemMessageUtility);
        systemValidator.validate();
    }

    @Test
    public void testValidateProviders() throws IOException {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("Black Duck URL"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Black Duck API Token");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        UserSystemValidator userSystemValidator = Mockito.mock(UserSystemValidator.class);
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);

        SystemMessageInitializer systemValidator = new SystemMessageInitializer(List.of(), settingsValidator, configurationAccessor, userSystemValidator, systemMessageUtility);
        systemValidator.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testvalidateBlackDuckProviderNullURL() {
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
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);

        String missingUrlMessageType = BlackDuckValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(defaultSystemMessageUtility);
        blackDuckValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq(String.format(BlackDuckValidator.MISSING_BLACKDUCK_URL_ERROR_W_CONFIG_FORMAT, DEFAULT_CONFIG_NAME)), Mockito.eq(SystemMessageSeverity.WARNING),
                Mockito.eq(missingUrlMessageType));
    }

    @Test
    public void testvalidateBlackDuckProviderLocalhostURL() {
        long configId = 1L;
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://localhost:443"));
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        Mockito.when(blackDuckProperties.getConfigName()).thenReturn(DEFAULT_CONFIG_NAME);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(configId);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);

        String localhostMessageType = BlackDuckValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(defaultSystemMessageUtility);
        blackDuckValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq(String.format(BlackDuckValidator.BLACKDUCK_LOCALHOST_ERROR_FORMAT, DEFAULT_CONFIG_NAME)), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(localhostMessageType));
    }

    @Test
    public void testValidateHubInvalidProvider() throws Exception {
        long configId = 1L;
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://localhost:443/alert"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Test Api Key");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);
        Mockito.when(blackDuckProperties.getConfigName()).thenReturn(DEFAULT_CONFIG_NAME);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(configId);

        BlackDuckServerConfig serverConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(serverConfig.canConnect(Mockito.any(IntLogger.class))).thenReturn(false);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfig(Mockito.any(IntLogger.class))).thenReturn(Optional.of(serverConfig));
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);

        String localhostMessageType = BlackDuckValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        String connectivityMessageType = BlackDuckValidator.createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);

        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(defaultSystemMessageUtility);
        blackDuckValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq(String.format(BlackDuckValidator.BLACKDUCK_LOCALHOST_ERROR_FORMAT, DEFAULT_CONFIG_NAME)), Mockito.eq(SystemMessageSeverity.WARNING), Mockito.eq(localhostMessageType));
        Mockito.verify(defaultSystemMessageUtility)
            .addSystemMessage(Mockito.eq(String.format("Can not connect to the Black Duck server with the configuration '%s'.", DEFAULT_CONFIG_NAME)), Mockito.eq(SystemMessageSeverity.WARNING),
                Mockito.eq(connectivityMessageType));
    }

    @Test
    public void testValidateHubValidProvider() throws Exception {
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://alert.example.com:443/alert"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Test Api Key");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);

        BlackDuckServerConfig serverConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(serverConfig.canConnect(Mockito.any(IntLogger.class))).thenReturn(true);
        Mockito.when(blackDuckProperties.createBlackDuckServerConfig(Mockito.any(IntLogger.class))).thenReturn(Optional.of(serverConfig));
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);

        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(defaultSystemMessageUtility);
        blackDuckValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }

    @Test
    public void testValidateHubValidProviderWithProxy() {
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
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("Black Duck URL"));
        Mockito.when(blackDuckProperties.getApiToken()).thenReturn("Black Duck API Token");
        Mockito.when(blackDuckProperties.getBlackDuckTimeout()).thenReturn(BlackDuckProperties.DEFAULT_TIMEOUT);

        BlackDuckServerConfig serverConfig = Mockito.mock(BlackDuckServerConfig.class);
        Mockito.when(serverConfig.canConnect(Mockito.any(IntLogger.class))).thenReturn(false);
        DefaultSystemMessageUtility defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);

        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider provider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(provider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);

        BlackDuckValidator blackDuckValidator = new BlackDuckValidator(defaultSystemMessageUtility);
        blackDuckValidator.validate(blackDuckProperties);
        Mockito.verify(defaultSystemMessageUtility, Mockito.times(0)).addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));
    }
}
