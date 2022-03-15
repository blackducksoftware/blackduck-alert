package com.synopsys.integration.alert.component.settings.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;

class ProxySettingsEnvironmentVariableHandlerTest {
    private final SettingsProxyValidator validator = new SettingsProxyValidator();

    @Test
    void testProxySetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        SettingsProxyConfigAccessor configAccessor = Mockito.mock(SettingsProxyConfigAccessor.class);

        Mockito.when(configAccessor.getConfiguration()).thenReturn(Optional.empty());
        Set<String> expectedVariableNames = ProxySettingsEnvironmentVariableHandler.PROXY_CONFIGURATION_KEYSET;

        String proxyHost = "https://proxyHostUrl";
        String proxyPort = "3128";
        String proxyUsername = "testUser";
        String proxyPassword = "testPassword";
        String nonProxyHosts = "https://nonProxyHostUrl";

        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentVariableHandler.PROXY_HOST_KEY, proxyHost);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentVariableHandler.PROXY_PORT_KEY, proxyPort);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentVariableHandler.PROXY_USERNAME_KEY, proxyUsername);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentVariableHandler.PROXY_PASSWORD_KEY, proxyPassword);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentVariableHandler.PROXY_NON_PROXY_HOSTS_KEY, nonProxyHosts);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        ProxySettingsEnvironmentVariableHandler proxySettingsEnvironmentVariableHandler = new ProxySettingsEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = proxySettingsEnvironmentVariableHandler.updateFromEnvironment();

        assertEquals(ProxySettingsEnvironmentVariableHandler.HANDLER_NAME, proxySettingsEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, proxySettingsEnvironmentVariableHandler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(proxyHost, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_HOST_KEY).orElse("Proxy host value is missing"));
        assertEquals(proxyPort, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_PORT_KEY).orElse("Proxy port value is missing"));
        assertEquals(proxyUsername, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_USERNAME_KEY).orElse("Proxy username value is missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_PASSWORD_KEY).orElse("Proxy password value is missing"));
        assertEquals(List.of(nonProxyHosts).toString(), result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_NON_PROXY_HOSTS_KEY).orElse("Proxy non-proxy hosts value is missing"));
    }

    @Test
    void testProxyMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        SettingsProxyConfigAccessor configAccessor = Mockito.mock(SettingsProxyConfigAccessor.class);
        Mockito.when(configAccessor.getConfiguration()).thenReturn(Optional.empty());
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        ProxySettingsEnvironmentVariableHandler proxySettingsEnvironmentVariableHandler = new ProxySettingsEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = proxySettingsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ProxySettingsEnvironmentVariableHandler.HANDLER_NAME, proxySettingsEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }
}
