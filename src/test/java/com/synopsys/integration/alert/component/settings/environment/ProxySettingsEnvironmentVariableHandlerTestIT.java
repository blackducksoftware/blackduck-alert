package com.synopsys.integration.alert.component.settings.environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AlertIntegrationTest
class ProxySettingsEnvironmentVariableHandlerTestIT {
    private static final String TEST_PROXY_HOST = "https://proxyHostUrl";
    private static final String TEST_PROXY_PORT = "3128";
    private static final String TEST_PROXY_USERNAME = "testUser";
    private static final String TEST_PROXY_PASSWORD = "testPassword";
    private static final List<String> nonProxyHostList = List.of("https://nonProxyHostUrl", "https://anotherNonProxyHostUrl");
    private static final String TEST_NON_PROXY_HOSTS = StringUtils.join(nonProxyHostList, ",");

    @Autowired
    private SettingsProxyConfigAccessor configAccessor;
    @Autowired
    private SettingsProxyValidator validator;

    @AfterEach
    @BeforeEach
    public void cleanup() {
        if (configAccessor.doesConfigurationExist()) {
            configAccessor.deleteConfiguration();
        }
    }

    @Test
    void testCleanEnvironment() {
        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        ProxySettingsEnvironmentVariableHandler proxySettingsEnvironmentVariableHandler = new ProxySettingsEnvironmentVariableHandler(
            configAccessor,
            environmentVariableUtility,
            validator
        );
        EnvironmentProcessingResult result = proxySettingsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ProxySettingsEnvironmentVariableHandler.HANDLER_NAME, proxySettingsEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues());

        assertEquals(TEST_PROXY_HOST, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_HOST_KEY).orElse("Proxy host name missing"));
        assertEquals(TEST_PROXY_PORT, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_PORT_KEY).orElse("Proxy port missing"));
        assertEquals(TEST_PROXY_USERNAME, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_USERNAME_KEY).orElse("Proxy username missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_PASSWORD_KEY).orElse("Proxy password missing"));
        assertEquals(nonProxyHostList.toString(), result.getVariableValue(ProxySettingsEnvironmentVariableHandler.PROXY_NON_PROXY_HOSTS_KEY).orElse("Non proxy hosts missing"));
    }

    @Test
    void testExistingConfig() throws AlertConfigurationException {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, TEST_PROXY_HOST, Integer.valueOf(TEST_PROXY_PORT));
        settingsProxyModel.setProxyUsername(TEST_PROXY_USERNAME);
        settingsProxyModel.setProxyPassword(TEST_PROXY_PASSWORD);
        settingsProxyModel.setNonProxyHosts(List.of(TEST_NON_PROXY_HOSTS));

        configAccessor.createConfiguration(settingsProxyModel);

        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        ProxySettingsEnvironmentVariableHandler proxySettingsEnvironmentVariableHandler = new ProxySettingsEnvironmentVariableHandler(
            configAccessor,
            environmentVariableUtility,
            validator
        );
        EnvironmentProcessingResult result = proxySettingsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ProxySettingsEnvironmentVariableHandler.HANDLER_NAME, proxySettingsEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    private Environment setupMockedEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Predicate<String> hasEnvVarCheck = (variableName) -> !ProxySettingsEnvironmentVariableHandler.PROXY_CONFIGURATION_KEYSET.contains(variableName);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, ProxySettingsEnvironmentVariableHandler.PROXY_HOST_KEY, TEST_PROXY_HOST);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, ProxySettingsEnvironmentVariableHandler.PROXY_PORT_KEY, TEST_PROXY_PORT);
        EnvironmentVariableMockingUtil
            .addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, ProxySettingsEnvironmentVariableHandler.PROXY_USERNAME_KEY, TEST_PROXY_USERNAME);
        EnvironmentVariableMockingUtil
            .addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, ProxySettingsEnvironmentVariableHandler.PROXY_PASSWORD_KEY, TEST_PROXY_PASSWORD);
        EnvironmentVariableMockingUtil
            .addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, ProxySettingsEnvironmentVariableHandler.PROXY_NON_PROXY_HOSTS_KEY, TEST_NON_PROXY_HOSTS);
        return environment;
    }
}
