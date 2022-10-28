package com.synopsys.integration.alert.channel.azure.boards.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

@ExtendWith(SpringExtension.class)
class AzureBoardsGlobalConfigurationModelConverterTest {
    private static final String TEST_ORGANIZATION_NAME = "testOrganizationName";
    private static final String TEST_CLIENT_ID = "testClientID";
    private static final String TEST_CLIENT_SECRET = "testClientSecret";

    private AzureBoardsGlobalConfigurationValidator validator;
    @Mock
    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;

    @BeforeEach
    public void init() {
        Mockito.when(azureBoardsGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        validator = new AzureBoardsGlobalConfigurationValidator(azureBoardsGlobalConfigAccessor);
    }

    @Test
    void validConversionTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        AzureBoardsGlobalConfigurationModelConverter converter = new AzureBoardsGlobalConfigurationModelConverter(validator);
        Optional<AzureBoardsGlobalConfigModel> model = converter.convertAndValidate(configurationModel);
        assertTrue(model.isPresent());
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = model.get();

        assertNull(azureBoardsGlobalConfigModel.getId());
        assertEquals(TEST_ORGANIZATION_NAME, azureBoardsGlobalConfigModel.getOrganizationName());
        assertEquals(TEST_CLIENT_ID, azureBoardsGlobalConfigModel.getAppId().orElse("Client ID is missing"));
        assertEquals(TEST_CLIENT_SECRET, azureBoardsGlobalConfigModel.getClientSecret().orElse("Client Secret is missing"));
    }

    @Test
    void emptyFieldsTest() {
        ConfigurationModel emptyModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, Map.of());
        AzureBoardsGlobalConfigurationModelConverter converter = new AzureBoardsGlobalConfigurationModelConverter(validator);
        Optional<AzureBoardsGlobalConfigModel> model = converter.convertAndValidate(emptyModel);
        assertTrue(model.isEmpty());
    }

    @Test
    void invalidPropertyKeyTest() {
        String invalidFieldKey = "invalid.jira.field";
        ConfigurationFieldModel invalidField = ConfigurationFieldModel.create(invalidFieldKey);
        Map<String, ConfigurationFieldModel> fieldValues = Map.of(invalidFieldKey, invalidField);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValues);
        AzureBoardsGlobalConfigurationModelConverter converter = new AzureBoardsGlobalConfigurationModelConverter(validator);
        Optional<AzureBoardsGlobalConfigModel> model = converter.convertAndValidate(configurationModel);
        assertTrue(model.isEmpty());
    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel organizationNameField = ConfigurationFieldModel.create(AzureBoardsGlobalConfigurationModelConverter.ORGANIZATION_NAME);
        ConfigurationFieldModel clientIdField = ConfigurationFieldModel.create(AzureBoardsGlobalConfigurationModelConverter.CLIENT_ID);
        ConfigurationFieldModel clientSecretField = ConfigurationFieldModel.create(AzureBoardsGlobalConfigurationModelConverter.CLIENT_SECRET);

        organizationNameField.setFieldValue(TEST_ORGANIZATION_NAME);
        clientIdField.setFieldValue(TEST_CLIENT_ID);
        clientSecretField.setFieldValue(TEST_CLIENT_SECRET);
        fieldValuesMap.put(AzureBoardsGlobalConfigurationModelConverter.ORGANIZATION_NAME, organizationNameField);
        fieldValuesMap.put(AzureBoardsGlobalConfigurationModelConverter.CLIENT_ID, clientIdField);
        fieldValuesMap.put(AzureBoardsGlobalConfigurationModelConverter.CLIENT_SECRET, clientSecretField);
        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }
}
