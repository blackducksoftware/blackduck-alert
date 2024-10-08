package com.synopsys.integration.alert.channel.azure.boards.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.env.MockEnvironment;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.environment.AzureBoardsEnvironmentVariableHandler;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class AzureBoardsEnvironmentVariableHandlerTestIT {
    private final String TEST_APP_ID = String.valueOf(UUID.randomUUID());
    private final String TEST_CLIENT_SECRET = "testSecret";
    private final String TEST_ORGANIZATION_NAME = "test-azure";

    @Autowired
    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    @Autowired
    private AzureBoardsGlobalConfigurationValidator validator;

    private MockEnvironment mockEnvironment;
    private EnvironmentVariableUtility environmentVariableUtility;

    @BeforeEach
    public void initEach() {
        mockEnvironment = new MockEnvironment();
        environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
    }

    @AfterEach
    public void cleanup() {
        azureBoardsGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(Config::getId)
            .map(UUID::fromString)
            .ifPresent(azureBoardsGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    void updateSetsVariablesOnCleanEnvironment() {
        mockEnvironment.setProperty(AzureBoardsEnvironmentVariableHandler.APP_ID_KEY, TEST_APP_ID);
        mockEnvironment.setProperty(AzureBoardsEnvironmentVariableHandler.CLIENT_SECRET_KEY, TEST_CLIENT_SECRET);
        mockEnvironment.setProperty(AzureBoardsEnvironmentVariableHandler.ORGANIZATION_NAME_KEY, TEST_ORGANIZATION_NAME);

        AzureBoardsEnvironmentVariableHandler azureBoardsEnvironmentVariableHandler = new AzureBoardsEnvironmentVariableHandler(azureBoardsGlobalConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = azureBoardsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.AZURE_BOARDS.getDisplayName(), azureBoardsEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues());

        assertEquals(TEST_ORGANIZATION_NAME, result.getVariableValue(AzureBoardsEnvironmentVariableHandler.ORGANIZATION_NAME_KEY).orElse("Organization Name value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(AzureBoardsEnvironmentVariableHandler.APP_ID_KEY).orElse("App Id value missing or not masked"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(AzureBoardsEnvironmentVariableHandler.CLIENT_SECRET_KEY).orElse("Client Secret value missing or not masked"));
    }

    @Test
    void updateReturnsNoResultsOnExistingConfig() throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            "",
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "",
            "",
            TEST_ORGANIZATION_NAME,
            TEST_APP_ID,
            Boolean.FALSE,
            TEST_CLIENT_SECRET,
            Boolean.FALSE
        );

        azureBoardsGlobalConfigAccessor.createConfiguration(azureBoardsGlobalConfigModel);
        assertEquals(1, azureBoardsGlobalConfigAccessor.getConfigurationCount());

        AzureBoardsEnvironmentVariableHandler azureBoardsEnvironmentVariableHandler = new AzureBoardsEnvironmentVariableHandler(azureBoardsGlobalConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = azureBoardsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.AZURE_BOARDS.getDisplayName(), azureBoardsEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }
}
