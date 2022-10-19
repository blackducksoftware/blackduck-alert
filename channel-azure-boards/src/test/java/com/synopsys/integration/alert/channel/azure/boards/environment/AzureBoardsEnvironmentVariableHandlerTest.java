package com.synopsys.integration.alert.channel.azure.boards.environment;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureBoardsEnvironmentVariableHandlerTest {
    private final String ORGANIZATION_NAME = "A Organization Name";
    private final String APP_ID = String.valueOf(UUID.randomUUID());
    private final String CLIENT_SECRET = "aClientSecret";

    private final Gson gson = new Gson();
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
        azureBoardsEnvironmentVariableHandler = new AzureBoardsEnvironmentVariableHandler(azureBoardsGlobalConfigAccessor, environmentVariableUtility, azureBoardsGlobalConfigurationValidator);
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
