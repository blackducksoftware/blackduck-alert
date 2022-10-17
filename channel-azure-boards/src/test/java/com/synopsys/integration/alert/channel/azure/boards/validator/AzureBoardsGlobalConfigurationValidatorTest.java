package com.synopsys.integration.alert.channel.azure.boards.validator;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AzureBoardsGlobalConfigurationValidatorTest {
    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    AzureBoardsGlobalConfigurationValidator azureBoardsGlobalConfigurationValidator;

    @BeforeEach
    void initEach() {
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);
        azureBoardsGlobalConfigurationValidator = new AzureBoardsGlobalConfigurationValidator(azureBoardsGlobalConfigAccessor);
    }

    @Test
    void validateReturnsNoErrorsOnCreateAndUpdate() throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel createModel = new AzureBoardsGlobalConfigModel(
            "",
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "",
            "",
            "Organization name",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "A secret",
            Boolean.TRUE
        );

        ValidationResponseModel createResponseModel = azureBoardsGlobalConfigurationValidator.validate(createModel, null);
        assertTrue(createResponseModel.getErrors().isEmpty());

        String createdModelId = azureBoardsGlobalConfigAccessor.createConfiguration(createModel).getId();
        AzureBoardsGlobalConfigModel updateModel = new AzureBoardsGlobalConfigModel(
            createdModelId,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "",
            "",
            "New organization name",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "New secret",
            Boolean.TRUE
        );

        ValidationResponseModel updateResponseModel = azureBoardsGlobalConfigurationValidator.validate(updateModel, createdModelId);
        assertTrue(updateResponseModel.getErrors().isEmpty());
    }

    @Test
    void validateReturnsAllMissingFieldErrors() {
        String modelId = String.valueOf(UUID.randomUUID());
        AzureBoardsGlobalConfigModel missingFieldsModel = new AzureBoardsGlobalConfigModel(modelId, "", "", "", "");

        ValidationResponseModel responseModel = azureBoardsGlobalConfigurationValidator.validate(missingFieldsModel, modelId);

        assertFalse(responseModel.getErrors().values().size() == 0);
        responseModel.getErrors().values()
            .forEach((alertFieldStatus) -> assertEquals(AlertFieldStatusMessages.REQUIRED_FIELD_MISSING, alertFieldStatus.getFieldMessage()));
    }

    @Test
    void validateReturnsErrorOnCreateDuplicateConfigName() throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel validModel = new AzureBoardsGlobalConfigModel(
            String.valueOf(UUID.randomUUID()),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "",
            "",
            "Organization name",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "A secret",
            Boolean.TRUE
        );
        azureBoardsGlobalConfigAccessor.createConfiguration(validModel);
        // Validate on already existing config name model should error on id param == null and != config name model id
        ValidationResponseModel nullIdResponseModel = azureBoardsGlobalConfigurationValidator.validate(validModel, null);

        String nullIdErrorMessage = nullIdResponseModel.getErrors().values().stream().findFirst().orElseThrow().getFieldMessage();
        assertEquals(AlertFieldStatusMessages.DUPLICATE_NAME_FOUND, nullIdErrorMessage);

        ValidationResponseModel differentIdResponseModel = azureBoardsGlobalConfigurationValidator.validate(validModel, String.valueOf(UUID.randomUUID()));

        String differentIdErrorMessage = differentIdResponseModel.getErrors().values().stream().findFirst().orElseThrow().getFieldMessage();
        assertEquals(AlertFieldStatusMessages.DUPLICATE_NAME_FOUND, differentIdErrorMessage);
    }
}
