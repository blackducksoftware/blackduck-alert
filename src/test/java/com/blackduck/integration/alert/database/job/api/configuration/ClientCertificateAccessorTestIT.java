/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.persistence.model.ClientCertificateModel;
import com.blackduck.integration.alert.database.job.api.ClientCertificateAccessor;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class ClientCertificateAccessorTestIT {
    @Autowired
    ClientCertificateAccessor accessor;

    @AfterEach
    public void cleanUp() {
        if (accessor.doesConfigurationExist()) {
            accessor.deleteConfiguration();
        }
        assertFalse(accessor.doesConfigurationExist());
    }

    @Test
    void getConfiguration() throws AlertConfigurationException {
        ClientCertificateModel createdModel =
            accessor.createConfiguration(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        ClientCertificateModel getModel = accessor.getConfiguration()
            .orElseThrow(() -> new AlertConfigurationException("Configuration does not exist"));

        assertEquals(createdModel.getKeyPassword(), getModel.getKeyPassword());
        assertEquals(createdModel.getKeyContent(), getModel.getKeyContent());
        assertEquals(createdModel.getClientCertificateContent(), getModel.getClientCertificateContent());
    }

    @Test
    void doesConfigurationExist() throws AlertConfigurationException {
        Assertions.assertFalse(accessor.doesConfigurationExist());

        accessor.createConfiguration(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        assertTrue(accessor.doesConfigurationExist());
    }

    @Test
    void createConfigurationThrowsOnExistingConfig() throws AlertConfigurationException {
        accessor.createConfiguration(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        assertTrue(accessor.doesConfigurationExist());

        ClientCertificateModel duplicateCreateModel =
            new ClientCertificateModel("new_key_password", "new_key_content", "new_certificate_content");
        assertThrows(AlertConfigurationException.class, () -> accessor.createConfiguration(duplicateCreateModel));

        ClientCertificateModel getModel = accessor.getConfiguration()
            .orElseThrow(() -> new AlertConfigurationException("Configuration does not exist"));

        assertNotEquals(getModel.getKeyPassword(), duplicateCreateModel.getKeyPassword());
        assertNotEquals(getModel.getKeyContent(), duplicateCreateModel.getKeyContent());
        assertNotEquals(getModel.getClientCertificateContent(), duplicateCreateModel.getClientCertificateContent());
    }

    @Test
    void updateConfiguration() throws AlertConfigurationException {
        ClientCertificateModel createdModel =
            accessor.createConfiguration(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        ClientCertificateModel updatedModel = accessor.updateConfiguration(
            new ClientCertificateModel(
                "new_key_password",
                "new_key_content",
                "certificate_content"   // unchanged
            )
        );

        Assertions.assertNotEquals(createdModel.getKeyPassword(), updatedModel.getKeyPassword());
        Assertions.assertNotEquals(createdModel.getKeyContent(), updatedModel.getKeyContent());

        Assertions.assertEquals(createdModel.getClientCertificateContent(), updatedModel.getClientCertificateContent());
    }

    @Test
    void deleteConfiguration() throws AlertConfigurationException {
        accessor.createConfiguration(new ClientCertificateModel("key_password", "key_content", "certificate_content"));
        assertTrue(accessor.doesConfigurationExist());
        accessor.deleteConfiguration();

        Assertions.assertFalse(accessor.doesConfigurationExist());
    }
}
