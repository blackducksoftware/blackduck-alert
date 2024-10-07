package com.blackduck.integration.alert.channel.azure.boards.environment;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.blackduck.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.database.MockRepositorySorter;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureBoardsEnvironmentVariableHandlerTest {
    private final String ORGANIZATION_NAME = "A Organization Name";
    private final String APP_ID = String.valueOf(UUID.randomUUID());
    private final String CLIENT_SECRET = "aClientSecret";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    AzureBoardsEnvironmentVariableHandler azureBoardsEnvironmentVariableHandler;
    AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    AzureBoardsGlobalConfigurationValidator azureBoardsGlobalConfigurationValidator;

    MockEnvironment mockEnvironment;

    @BeforeEach
    void initEach() {
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);
        azureBoardsGlobalConfigurationValidator = new AzureBoardsGlobalConfigurationValidator(azureBoardsGlobalConfigAccessor);

        mockEnvironment = new MockEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        azureBoardsEnvironmentVariableHandler = new AzureBoardsEnvironmentVariableHandler(
            azureBoardsGlobalConfigAccessor,
            environmentVariableUtility,
            azureBoardsGlobalConfigurationValidator
        );
    }

    @Test
    void updateFromEnvironmentCreatesConfigurationModel() throws Exception {
        mockEnvironment.setProperty(AzureBoardsEnvironmentVariableHandler.APP_ID_KEY, APP_ID);
        mockEnvironment.setProperty(AzureBoardsEnvironmentVariableHandler.CLIENT_SECRET_KEY, CLIENT_SECRET);
        mockEnvironment.setProperty(AzureBoardsEnvironmentVariableHandler.ORGANIZATION_NAME_KEY, ORGANIZATION_NAME);

        assertTrue(azureBoardsEnvironmentVariableHandler.configurationMissingCheck());
        EnvironmentProcessingResult result = azureBoardsEnvironmentVariableHandler.updateFromEnvironment();
        assertFalse(azureBoardsEnvironmentVariableHandler.configurationMissingCheck());

        AzureBoardsGlobalConfigModel configModel = azureBoardsGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .orElseThrow(() -> new Exception("Config model not found"));
        assertEquals(ORGANIZATION_NAME, configModel.getOrganizationName());

        assertTrue(result.hasValues());
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(AzureBoardsEnvironmentVariableHandler.APP_ID_KEY).orElse("App id is missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(AzureBoardsEnvironmentVariableHandler.CLIENT_SECRET_KEY).orElse("Client secret is missing"));
        assertEquals(ORGANIZATION_NAME, result.getVariableValue(AzureBoardsEnvironmentVariableHandler.ORGANIZATION_NAME_KEY).orElse("Organization name is missing"));
    }

    @Test
    void updateFromEnvironmentReturnsNoValuesWhenMissingEnvironmentVariables() {
        AzureBoardsEnvironmentVariableHandler.VARIABLE_NAMES
            .forEach(variableName -> assertFalse(mockEnvironment.containsProperty(variableName)));

        EnvironmentProcessingResult result = azureBoardsEnvironmentVariableHandler.updateFromEnvironment();
        assertTrue(azureBoardsEnvironmentVariableHandler.configurationMissingCheck());

        assertFalse(result.hasValues());
    }

    @Test
    void updateFromEnvironmentReturnsNoValuesWhenConfigurationsExists() throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel modelToCreate = new AzureBoardsGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            ORGANIZATION_NAME,
            APP_ID,
            CLIENT_SECRET
        );
        azureBoardsGlobalConfigAccessor.createConfiguration(modelToCreate);
        assertEquals(1, azureBoardsGlobalConfigAccessor.getConfigurationCount());

        EnvironmentProcessingResult result = azureBoardsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(1, azureBoardsGlobalConfigAccessor.getConfigurationCount());

        assertFalse(result.hasValues());
    }
}
