package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockClientCertificateRepository;

class DefaultClientCertificateAccessorTest {
    private DefaultClientCertificateAccessor clientCertificateAccessor;

    private ClientCertificateModel clientCertificateModel;

    @BeforeEach
    void init() {
        MockClientCertificateRepository mockClientCertificateRepository = new MockClientCertificateRepository();
        clientCertificateAccessor = new DefaultClientCertificateAccessor(mockClientCertificateRepository);
        clientCertificateModel = new ClientCertificateModel(null, "alias", UUID.randomUUID(), "certificate_content",
                DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }

    @Test
    void getCertificate() throws AlertConfigurationException {
        ClientCertificateModel savedModel = clientCertificateAccessor.createConfiguration(clientCertificateModel);
        ClientCertificateModel queryModel = clientCertificateAccessor.getConfiguration()
                .orElseThrow(() -> new AlertConfigurationException("Model does not exist."));

        assertEquals(savedModel.getId(), queryModel.getId());
    }

    @Test
    void doesConfigurationExist() throws AlertConfigurationException {
        assertFalse(clientCertificateAccessor.doesConfigurationExist());

        clientCertificateAccessor.createConfiguration(clientCertificateModel);
        assertTrue(clientCertificateAccessor.doesConfigurationExist());
    }

    @Test
    void createConfigurationThrowsOnExistingConfig() throws AlertConfigurationException {
        UUID createdId = clientCertificateAccessor.createConfiguration(clientCertificateModel).getId();
        assertTrue(clientCertificateAccessor.doesConfigurationExist());

        ClientCertificateModel duplicateCreateModel = new ClientCertificateModel(null, "new_alias", UUID.randomUUID(), "new_certificate_content",
                DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        assertThrows(AlertConfigurationException.class, () -> clientCertificateAccessor.createConfiguration(duplicateCreateModel));
        UUID currentConfigId = clientCertificateAccessor.getConfiguration().orElseThrow().getId();
        assertNotEquals(currentConfigId, duplicateCreateModel.getId());
        assertEquals(currentConfigId, createdId);
    }

    @Test
    void updateConfiguration() throws AlertConfigurationException {
        ClientCertificateModel createdModel = clientCertificateAccessor.createConfiguration(clientCertificateModel);

        ClientCertificateModel changedModel = new ClientCertificateModel(createdModel.getId(), "new_alias", createdModel.getPrivateKeyId(),
                "new_certificate_content", DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ClientCertificateModel updatedModel = clientCertificateAccessor.updateConfiguration(changedModel);

        assertEquals(changedModel.getId(), updatedModel.getId());
        assertEquals(changedModel.getAlias(), updatedModel.getAlias());
        assertEquals(changedModel.getCertificateContent(), updatedModel.getCertificateContent());

        assertNotEquals(createdModel.getAlias(), updatedModel.getAlias());
        assertNotEquals(createdModel.getCertificateContent(), updatedModel.getCertificateContent());
    }

    @Test
    void deleteConfiguration() throws AlertConfigurationException {
        clientCertificateAccessor.createConfiguration(clientCertificateModel);
        clientCertificateAccessor.deleteConfiguration();

        assertFalse(clientCertificateAccessor.doesConfigurationExist());
    }
}
