package com.synopsys.integration.alert.channel.azure.boards.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

class AzureBoardsGlobalConfigExistsValidatorTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;

    @BeforeEach
    public void init() {
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(
            new MockRepositorySorter<>()
        );
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);
    }

    @Test
    void existsReturnsTrueOnAtLeastOneConfiguration() throws AlertConfigurationException {
        AzureBoardsGlobalConfigExistsValidator validator = new AzureBoardsGlobalConfigExistsValidator(
            ChannelKeys.AZURE_BOARDS,
            azureBoardsGlobalConfigAccessor
        );

        createAzureBoardsConfigModel(1);
        assertTrue(validator.exists());
        createAzureBoardsConfigModel(5);
        assertTrue(validator.exists());
    }

    @Test
    void existsReturnsFalseOnNoConfiguration() {
        AzureBoardsGlobalConfigExistsValidator validator = new AzureBoardsGlobalConfigExistsValidator(
            ChannelKeys.AZURE_BOARDS,
            azureBoardsGlobalConfigAccessor
        );

        assertFalse(validator.exists());
    }

    private void createAzureBoardsConfigModel(int count) throws AlertConfigurationException {
        for (int i = 0; i < count; i++) {
            azureBoardsGlobalConfigAccessor.createConfiguration(
                new AzureBoardsGlobalConfigModel(
                    "",
                    String.valueOf(UUID.randomUUID()),
                    "",
                    "",
                    "First org",
                    String.valueOf(UUID.randomUUID()),
                    Boolean.TRUE,
                    "a secret",
                    Boolean.TRUE
                )
            );
        }

    }
}
