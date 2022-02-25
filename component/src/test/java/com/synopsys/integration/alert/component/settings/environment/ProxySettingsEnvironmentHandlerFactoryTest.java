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
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;

class ProxySettingsEnvironmentHandlerFactoryTest {
    private final SettingsProxyValidator validator = new SettingsProxyValidator();

    @Test
    void testProxySetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        SettingsProxyConfigAccessor configAccessor = Mockito.mock(SettingsProxyConfigAccessor.class);

        Mockito.when(configAccessor.getConfiguration()).thenReturn(Optional.empty());
        Set<String> expectedVariableNames = ProxySettingsEnvironmentHandlerFactory.PROXY_CONFIGURATION_KEYSET;

        String proxyHost = "https://proxyHostUrl";
        String proxyPort = "3128";
        String proxyUsername = "testUser";
        String proxyPassword = "testPassword";
        String nonProxyHosts = "https://nonProxyHostUrl";

        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentHandlerFactory.PROXY_HOST_KEY, proxyHost);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentHandlerFactory.PROXY_PORT_KEY, proxyPort);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentHandlerFactory.PROXY_USERNAME_KEY, proxyUsername);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentHandlerFactory.PROXY_PASSWORD_KEY, proxyPassword);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, (ignored) -> Boolean.TRUE, ProxySettingsEnvironmentHandlerFactory.PROXY_NON_PROXY_HOSTS_KEY, nonProxyHosts);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new ProxySettingsEnvironmentHandlerFactory(configAccessor, environmentVariableUtility, validator);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();

        assertEquals(ProxySettingsEnvironmentHandlerFactory.HANDLER_NAME, handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(proxyHost, result.getVariableValue(ProxySettingsEnvironmentHandlerFactory.PROXY_HOST_KEY).orElse("Proxy host value is missing"));
        assertEquals(proxyPort, result.getVariableValue(ProxySettingsEnvironmentHandlerFactory.PROXY_PORT_KEY).orElse("Proxy port value is missing"));
        assertEquals(proxyUsername, result.getVariableValue(ProxySettingsEnvironmentHandlerFactory.PROXY_USERNAME_KEY).orElse("Proxy username value is missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(ProxySettingsEnvironmentHandlerFactory.PROXY_PASSWORD_KEY).orElse("Proxy password value is missing"));
        assertEquals(List.of(nonProxyHosts).toString(), result.getVariableValue(ProxySettingsEnvironmentHandlerFactory.PROXY_NON_PROXY_HOSTS_KEY).orElse("Proxy non-proxy hosts value is missing"));
    }

    @Test
    void testProxyMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        SettingsProxyConfigAccessor configAccessor = Mockito.mock(SettingsProxyConfigAccessor.class);
        Mockito.when(configAccessor.getConfiguration()).thenReturn(Optional.empty());
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new ProxySettingsEnvironmentHandlerFactory(configAccessor, environmentVariableUtility, validator);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ProxySettingsEnvironmentHandlerFactory.HANDLER_NAME, handler.getName());
        assertFalse(result.hasValues());
    }
}
