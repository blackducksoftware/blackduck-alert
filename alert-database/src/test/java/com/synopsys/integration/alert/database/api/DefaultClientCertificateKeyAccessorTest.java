package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockClientCertificateKeyRepository;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

class DefaultClientCertificateKeyAccessorTest {
    private DefaultClientCertificateKeyAccessor clientCertificateKeyAccessor;

    private ClientCertificateKeyModel clientCertificateKeyModel;

    private MockClientCertificateKeyRepository mockClientCertificateKeyRepository;

    private EncryptionUtility encryptionUtility;

    @BeforeEach
    void init() {
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
        encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

        mockClientCertificateKeyRepository = new MockClientCertificateKeyRepository();
        clientCertificateKeyAccessor = new DefaultClientCertificateKeyAccessor(encryptionUtility, mockClientCertificateKeyRepository);
        clientCertificateKeyModel = new ClientCertificateKeyModel(null, "name", "password", true, "key_content",
                DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }

    @Test
    void getConfiguration() throws AlertConfigurationException {
        ClientCertificateKeyModel createdModel = clientCertificateKeyAccessor.createConfiguration(clientCertificateKeyModel);
        ClientCertificateKeyModel queryModel = clientCertificateKeyAccessor.getConfiguration()
                .orElseThrow(() -> new AlertConfigurationException("Model does not exist."));

        assertEquals(createdModel.getId(), queryModel.getId());
    }

    @Test
    void doesConfigurationExist() throws AlertConfigurationException {
        assertFalse(clientCertificateKeyAccessor.doesConfigurationExist());

        clientCertificateKeyAccessor.createConfiguration(clientCertificateKeyModel);
        assertTrue(clientCertificateKeyAccessor.doesConfigurationExist());
    }

    @Test
    void createConfigurationThrowsOnExistingConfig() throws AlertConfigurationException {
        UUID createdId = clientCertificateKeyAccessor.createConfiguration(clientCertificateKeyModel).getId();
        assertTrue(clientCertificateKeyAccessor.doesConfigurationExist());

        ClientCertificateKeyModel duplicateCreateModel = new ClientCertificateKeyModel(null, "new_name", "new_password",
                true, "new_key_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        assertThrows(AlertConfigurationException.class, () -> clientCertificateKeyAccessor.createConfiguration(duplicateCreateModel));
        UUID currentConfigId = clientCertificateKeyAccessor.getConfiguration().orElseThrow().getId();
        assertNotEquals(currentConfigId, duplicateCreateModel.getId());
        assertEquals(currentConfigId, createdId);
    }

    @Test
    void updateConfiguration() throws AlertConfigurationException {
        ClientCertificateKeyModel createdModel = clientCertificateKeyAccessor.createConfiguration(clientCertificateKeyModel);

        ClientCertificateKeyModel changedModel = new ClientCertificateKeyModel(createdModel.getId(), "new_name", createdModel.getPassword().orElse(""),
                createdModel.getIsPasswordSet(), "new_key_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ClientCertificateKeyModel updatedModel = clientCertificateKeyAccessor.updateConfiguration(changedModel);

        assertEquals(changedModel.getId(), updatedModel.getId());
        assertEquals(changedModel.getPassword(), changedModel.getPassword());
        assertEquals(changedModel.getIsPasswordSet(), updatedModel.getIsPasswordSet());

        assertNotEquals(createdModel.getName(), updatedModel.getName());
        assertNotEquals(createdModel.getKeyContent(), updatedModel.getKeyContent());
    }

    @Test
    void createAndUpdateConfigurationObfuscates() throws AlertConfigurationException {
        ClientCertificateKeyModel createdModel = clientCertificateKeyAccessor.createConfiguration(clientCertificateKeyModel);
        ClientCertificateKeyEntity createdEntity = mockClientCertificateKeyRepository.getById(createdModel.getId());

        Optional<String> optionalCreatedPassword = createdModel.getPassword();
        assertTrue(optionalCreatedPassword.isPresent());
        assertNotEquals(optionalCreatedPassword.get(), createdEntity.getPassword()); // Entity password is encrypted
        assertEquals(optionalCreatedPassword.get(), encryptionUtility.decrypt(createdEntity.getPassword()));

        ClientCertificateKeyModel changedModel = new ClientCertificateKeyModel(createdModel.getId(), "new_name", "new_password",
                true, "new_key_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ClientCertificateKeyModel updatedModel = clientCertificateKeyAccessor.updateConfiguration(changedModel);
        ClientCertificateKeyEntity updatedEntity = mockClientCertificateKeyRepository.getById(createdModel.getId());

        Optional<String> optionalUpdatedPassword = updatedModel.getPassword();
        assertTrue(optionalUpdatedPassword.isPresent());
        assertNotEquals(optionalUpdatedPassword.get(), updatedEntity.getPassword()); // Entity password is encrypted
        assertEquals(optionalUpdatedPassword.get(), encryptionUtility.decrypt(updatedEntity.getPassword()));

        // Password is updated
        assertNotEquals(createdEntity.getPassword(), updatedEntity.getPassword());
        assertNotEquals(optionalCreatedPassword.get(), optionalUpdatedPassword.get());
    }

    @Test
    void deleteConfiguration() throws AlertConfigurationException {
        clientCertificateKeyAccessor.createConfiguration(clientCertificateKeyModel);
        clientCertificateKeyAccessor.deleteConfiguration();

        assertFalse(clientCertificateKeyAccessor.doesConfigurationExist());
    }
}
