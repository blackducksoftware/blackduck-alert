package com.synopsys.integration.alert.component.settings.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

class ProxySettingsEnvironmentHandlerFactoryTest {
    @Test
    void testProxySetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        SettingsProxyConfigAccessor configAccessor = Mockito.mock(SettingsProxyConfigAccessor.class);

        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        Mockito.when(configAccessor.getConfiguration()).thenReturn(Optional.of(settingsProxyModel));
        Set<String> expectedVariableNames = ProxySettingsEnvironmentHandlerFactory.PROXY_CONFIGURATION_KEYSET;

        String proxyHost = "https://proxyHostUrl";
        String proxyPort = "3128";
        String proxyUsername = "testUser";
        String proxyPassword = "testPassword";
        String nonProxyHosts = "https://nonProxyHostUrl";

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(environment.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_HOST_KEY)).thenReturn(proxyHost);
        Mockito.when(environment.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_PORT_KEY)).thenReturn(proxyPort);
        Mockito.when(environment.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_USERNAME_KEY)).thenReturn(proxyUsername);
        Mockito.when(environment.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_PASSWORD_KEY)).thenReturn(proxyPassword);
        Mockito.when(environment.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_NON_PROXY_HOSTS_KEY)).thenReturn(nonProxyHosts);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new ProxySettingsEnvironmentHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();

        assertEquals(ProxySettingsEnvironmentHandlerFactory.HANDLER_NAME, handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(proxyHost, updatedProperties.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_HOST_KEY));
        assertEquals(proxyPort, updatedProperties.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_PORT_KEY));
        assertEquals(proxyUsername, updatedProperties.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_USERNAME_KEY));
        assertNull(updatedProperties.getProperty(ProxySettingsEnvironmentHandlerFactory.PROXY_PASSWORD_KEY));
        assertEquals(List.of(nonProxyHosts), updatedProperties.get(ProxySettingsEnvironmentHandlerFactory.PROXY_NON_PROXY_HOSTS_KEY));
    }

    @Test
    void testProxyMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        SettingsProxyConfigAccessor configAccessor = Mockito.mock(SettingsProxyConfigAccessor.class);
        Mockito.when(configAccessor.getConfiguration()).thenReturn(Optional.empty());
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new ProxySettingsEnvironmentHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ProxySettingsEnvironmentHandlerFactory.HANDLER_NAME, handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }
}
