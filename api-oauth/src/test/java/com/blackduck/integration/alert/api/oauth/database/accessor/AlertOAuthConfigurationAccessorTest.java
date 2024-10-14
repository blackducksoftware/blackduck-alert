/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.oauth.database.accessor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.oauth.database.AlertOAuthModel;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class AlertOAuthConfigurationAccessorTest {
    private MockAlertOAuthConfigurationRepository repository;
    private AlertOAuthConfigurationAccessor accessor;

    @BeforeEach
    void initRepository() {
        repository = new MockAlertOAuthConfigurationRepository();
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        accessor = new AlertOAuthConfigurationAccessor(repository, encryptionUtility);
    }

    @Test
    void readSingleConfiguration() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        AlertOAuthModel savedModel = accessor.getConfiguration(model.getId()).orElseThrow(() -> new AssertionError("Configuration with ID not found."));

        assertEquals(model.getId(), savedModel.getId());
        assertEquals(model.getAccessToken().orElse(null), savedModel.getAccessToken().orElseThrow(() -> new AssertionError("Access Token not found.")));
        assertEquals(model.getRefreshToken().orElse(null), savedModel.getRefreshToken().orElseThrow(() -> new AssertionError("Refresh Token not found.")));
        assertEquals(
            model.getExirationTimeMilliseconds().orElse(null),
            savedModel.getExirationTimeMilliseconds().orElseThrow(() -> new AssertionError("Expiration Time not found."))
        );
    }

    @Test
    void readSingleConfigurationWithUnknownId() {
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        accessor = new AlertOAuthConfigurationAccessor(repository, encryptionUtility);
        Optional<AlertOAuthModel> configuration = accessor.getConfiguration(UUID.randomUUID());
        assertTrue(configuration.isEmpty());
    }

    @Test
    void readAllConfigurations() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);

        List<AlertOAuthModel> models = accessor.getConfigurations();
        assertEquals(2, models.size());
    }

    @Test
    void createThrowsException() {
        AlertOAuthModel model = createTestModel();
        assertDoesNotThrow(() -> accessor.createConfiguration(model));
        assertThrows(AlertConfigurationException.class, () -> accessor.createConfiguration(model));
    }

    @Test
    void checkConfigurationDoesNotExist() throws AlertConfigurationException {
        UUID unknownId = UUID.randomUUID();
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);

        assertEquals(2, repository.count());
        assertFalse(accessor.existsConfigurationById(unknownId));
    }

    @Test
    void checkConfigurationExist() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        UUID knownId = model.getId();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);

        assertEquals(2, repository.count());
        assertTrue(accessor.existsConfigurationById(knownId));
    }

    @Test
    void updateConfigurationIfExists() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        String updatedAccessToken = "updateAccessToken";
        String updatedRefreshToken = "refreshAccessToken";
        Long updatedExpirationTime = 100L;
        AlertOAuthModel updatedModel = new AlertOAuthModel(model.getId(), updatedAccessToken, updatedRefreshToken, updatedExpirationTime);
        updatedModel = accessor.updateConfiguration(model.getId(), updatedModel).orElseThrow(() -> new AssertionError("Configuration with ID not found."));

        assertEquals(model.getId(), updatedModel.getId());
        assertEquals(updatedAccessToken, updatedModel.getAccessToken().orElseThrow(() -> new AssertionError("Access Token not found.")));
        assertEquals(updatedRefreshToken, updatedModel.getRefreshToken().orElseThrow(() -> new AssertionError("Refresh Token not found.")));
        assertEquals(updatedExpirationTime, updatedModel.getExirationTimeMilliseconds().orElseThrow(() -> new AssertionError("Expiration Time not found.")));
    }

    @Test
    void updateConfigurationIfNotExists() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        String updatedAccessToken = "updateAccessToken";
        String updatedRefreshToken = "refreshAccessToken";
        Long updatedExpirationTime = 100L;
        UUID unknownId = UUID.randomUUID();
        AlertOAuthModel updatedModel = new AlertOAuthModel(unknownId, updatedAccessToken, updatedRefreshToken, updatedExpirationTime);
        Optional<AlertOAuthModel> updated = accessor.updateConfiguration(unknownId, updatedModel);

        assertTrue(updated.isEmpty());
    }

    @Test
    void deleteConfiguration() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        accessor.deleteConfiguration(model.getId());
        assertTrue(accessor.getConfigurations().isEmpty());
    }

    @Test
    void deleteConfigurationDoesNotExist() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        accessor.deleteConfiguration(UUID.randomUUID());
        assertEquals(1, accessor.getConfigurations().size());
    }

    private AlertOAuthModel createTestModel() {
        UUID id = UUID.randomUUID();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Long expirationTime = 5000L;
        return new AlertOAuthModel(id, accessToken, refreshToken, expirationTime);
    }
}
