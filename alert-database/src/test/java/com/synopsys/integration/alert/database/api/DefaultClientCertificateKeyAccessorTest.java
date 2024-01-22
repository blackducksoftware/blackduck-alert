package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.ClientCertificateKeyAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockClientCertificateKeyRepository;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

class DefaultClientCertificateKeyAccessorTest {
    private ClientCertificateKeyAccessor clientCertificateKeyAccessor;

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
    void getCertificateKey() throws AlertConfigurationException {
        ClientCertificateKeyModel savedModel = clientCertificateKeyAccessor.saveCertificateKey(clientCertificateKeyModel);
        ClientCertificateKeyModel queryModel = clientCertificateKeyAccessor.getCertificateKey(savedModel.getId())
                .orElseThrow(() -> new AlertConfigurationException("Model does not exist."));

        assertEquals(savedModel.getId(), queryModel.getId());
    }

    @Test
    void saveCertificateUpdates() {
        ClientCertificateKeyModel savedModel = clientCertificateKeyAccessor.saveCertificateKey(clientCertificateKeyModel);

        ClientCertificateKeyModel changedModel = new ClientCertificateKeyModel(savedModel.getId(), "new_name", savedModel.getPassword().orElse(""),
                savedModel.getIsPasswordSet(), "new_key_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ClientCertificateKeyModel updatedModel = clientCertificateKeyAccessor.saveCertificateKey(changedModel);

        assertEquals(changedModel.getId(), updatedModel.getId());
        assertEquals(changedModel.getPassword(), changedModel.getPassword());
        assertEquals(changedModel.getIsPasswordSet(), updatedModel.getIsPasswordSet());

        assertNotEquals(savedModel.getName(), updatedModel.getName());
        assertNotEquals(savedModel.getKeyContent(), updatedModel.getKeyContent());
    }

    @Test
    void saveCertificateObfuscates() {
        ClientCertificateKeyModel savedModel = clientCertificateKeyAccessor.saveCertificateKey(clientCertificateKeyModel);
        ClientCertificateKeyEntity entity = mockClientCertificateKeyRepository.getById(savedModel.getId());

        assertNotEquals(savedModel.getPassword(), entity.getPassword());
        Optional<String> optionalPassword = savedModel.getPassword();
        assertTrue(optionalPassword.isPresent());
        assertEquals(optionalPassword.get(), encryptionUtility.decrypt(entity.getPassword()));
    }

    @Test
    void deleteCertificate() {
        ClientCertificateKeyModel savedModel = clientCertificateKeyAccessor.saveCertificateKey(clientCertificateKeyModel);
        clientCertificateKeyAccessor.deleteCertificateKey(savedModel.getId());

        assertTrue(clientCertificateKeyAccessor.getCertificateKey(savedModel.getId()).isEmpty());
    }
}
