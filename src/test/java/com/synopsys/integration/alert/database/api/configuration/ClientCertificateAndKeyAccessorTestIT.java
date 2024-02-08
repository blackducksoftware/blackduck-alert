package com.synopsys.integration.alert.database.api.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateAndKeyModel;
import com.synopsys.integration.alert.database.api.ClientCertificateAndKeyAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class ClientCertificateAndKeyAccessorTestIT {
    @Autowired
    ClientCertificateAndKeyAccessor accessor;

    @AfterEach
    public void cleanUp() {
        if (accessor.doesConfigurationExist()) {
            accessor.deleteConfiguration();
        }
        assertFalse(accessor.doesConfigurationExist());
    }

    @Test
    void getConfiguration() throws AlertConfigurationException {
        ClientCertificateAndKeyModel createdModel =
            accessor.createConfiguration(new ClientCertificateAndKeyModel("key_password", "key_content", "certificate_content"));
        ClientCertificateAndKeyModel getModel = accessor.getConfiguration()
            .orElseThrow(() -> new AlertConfigurationException("Configuration does not exist"));

        assertEquals(createdModel.getKeyPassword(), getModel.getKeyPassword());
        assertEquals(createdModel.getKeyContent(), getModel.getKeyContent());
        assertEquals(createdModel.getCertificateContent(), getModel.getCertificateContent());
    }

    @Test
    void doesConfigurationExist() throws AlertConfigurationException {
        Assertions.assertFalse(accessor.doesConfigurationExist());

        accessor.createConfiguration(new ClientCertificateAndKeyModel("key_password", "key_content", "certificate_content"));
        assertTrue(accessor.doesConfigurationExist());
    }

    @Test
    void createConfigurationThrowsOnExistingConfig() throws AlertConfigurationException {
        accessor.createConfiguration(new ClientCertificateAndKeyModel("key_password", "key_content", "certificate_content"));
        assertTrue(accessor.doesConfigurationExist());

        ClientCertificateAndKeyModel duplicateCreateModel =
            new ClientCertificateAndKeyModel("new_key_password", "new_key_content", "new_certificate_content");
        assertThrows(AlertConfigurationException.class, () -> accessor.createConfiguration(duplicateCreateModel));

        ClientCertificateAndKeyModel getModel = accessor.getConfiguration()
            .orElseThrow(() -> new AlertConfigurationException("Configuration does not exist"));

        assertNotEquals(getModel.getKeyPassword(), duplicateCreateModel.getKeyPassword());
        assertNotEquals(getModel.getKeyContent(), duplicateCreateModel.getKeyContent());
        assertNotEquals(getModel.getCertificateContent(), duplicateCreateModel.getCertificateContent());
    }

    @Test
    void updateConfiguration() throws AlertConfigurationException {
        ClientCertificateAndKeyModel createdModel =
            accessor.createConfiguration(new ClientCertificateAndKeyModel("key_password", "key_content", "certificate_content"));
        ClientCertificateAndKeyModel updatedModel = accessor.updateConfiguration(
            new ClientCertificateAndKeyModel(
                "new_key_password",
                "new_key_content",
                "certificate_content"   // unchanged
            )
        );

        Assertions.assertNotEquals(createdModel.getKeyPassword(), updatedModel.getKeyPassword());
        Assertions.assertNotEquals(createdModel.getKeyContent(), updatedModel.getKeyContent());

        Assertions.assertEquals(createdModel.getCertificateContent(), updatedModel.getCertificateContent());
    }

    @Test
    void deleteConfiguration() throws AlertConfigurationException {
        accessor.createConfiguration(new ClientCertificateAndKeyModel("key_password", "key_content", "certificate_content"));
        assertTrue(accessor.doesConfigurationExist());
        accessor.deleteConfiguration();

        Assertions.assertFalse(accessor.doesConfigurationExist());
    }
}
