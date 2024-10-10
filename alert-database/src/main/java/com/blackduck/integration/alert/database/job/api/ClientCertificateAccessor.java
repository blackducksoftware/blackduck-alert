/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import static com.blackduck.integration.alert.common.rest.AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS;
import static com.blackduck.integration.alert.common.rest.AlertRestConstants.DEFAULT_CONFIGURATION_NAME;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ClientCertificateModel;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.certificates.ClientCertificateEntity;
import com.blackduck.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.blackduck.integration.alert.database.certificates.ClientCertificateKeyRepository;
import com.blackduck.integration.alert.database.certificates.ClientCertificateRepository;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class ClientCertificateAccessor implements UniqueConfigurationAccessor<ClientCertificateModel> {
    private final EncryptionUtility encryptionUtility;
    private final ClientCertificateKeyRepository keyRepository;
    private final ClientCertificateRepository certificateRepository;

    public ClientCertificateAccessor(EncryptionUtility encryptionUtility, ClientCertificateKeyRepository keyRepository, ClientCertificateRepository certificateRepository) {
        this.encryptionUtility = encryptionUtility;
        this.keyRepository = keyRepository;
        this.certificateRepository = certificateRepository;
    }

    @Override
    public Optional<ClientCertificateModel> getConfiguration() {
        Optional<ClientCertificateKeyEntity> optionalKeyEntity = keyRepository.findByName(DEFAULT_CONFIGURATION_NAME);
        Optional<ClientCertificateEntity> optionalCertificateEntity = certificateRepository.findByAlias(DEFAULT_CLIENT_CERTIFICATE_ALIAS);

        if (optionalKeyEntity.isPresent() && optionalCertificateEntity.isPresent()) {
            return Optional.of(toModel(optionalKeyEntity.get(), optionalCertificateEntity.get()));
        }

        return Optional.empty();
    }

    @Override
    public boolean doesConfigurationExist() {
        return keyRepository.existsByName(DEFAULT_CONFIGURATION_NAME) && certificateRepository.existsByAlias(DEFAULT_CLIENT_CERTIFICATE_ALIAS);
    }

    @Override
    public ClientCertificateModel createConfiguration(ClientCertificateModel configuration) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException("A client certificate and key configuration already exists.");
        }

        Pair<ClientCertificateKeyEntity, ClientCertificateEntity> keyCertificateEntityPair =
            toEntity(UUID.randomUUID(), UUID.randomUUID(), configuration, DateUtils.createCurrentDateTimestamp());
        ClientCertificateKeyEntity savedKeyEntity = keyRepository.save(keyCertificateEntityPair.getLeft());
        ClientCertificateEntity savedCertificateEntity = certificateRepository.save(keyCertificateEntityPair.getRight());

        return toModel(savedKeyEntity, savedCertificateEntity);
    }

    @Override
    public ClientCertificateModel updateConfiguration(ClientCertificateModel configuration) throws AlertConfigurationException {
        ClientCertificateKeyEntity existingKeyEntity = keyRepository.findByName(DEFAULT_CONFIGURATION_NAME)
            .orElseThrow(() -> new AlertConfigurationException("Client certificate key does not exist"));
        ClientCertificateEntity existingCertificateEntity = certificateRepository.findByAlias(DEFAULT_CLIENT_CERTIFICATE_ALIAS)
            .orElseThrow(() -> new AlertConfigurationException("Client certificate does not exist"));

        Pair<ClientCertificateKeyEntity, ClientCertificateEntity> keyCertificateEntityPair = toEntity(
            existingKeyEntity.getId(),
            existingCertificateEntity.getId(),
            configuration,
            DateUtils.createCurrentDateTimestamp()
        );
        ClientCertificateKeyEntity savedKeyEntity = keyRepository.save(keyCertificateEntityPair.getLeft());
        ClientCertificateEntity savedCertificateEntity = certificateRepository.save(keyCertificateEntityPair.getRight());

        return toModel(savedKeyEntity, savedCertificateEntity);
    }

    @Override
    public void deleteConfiguration() {
        keyRepository.deleteByName(DEFAULT_CONFIGURATION_NAME);
    }

    private Pair<ClientCertificateKeyEntity, ClientCertificateEntity> toEntity(UUID keyId, UUID certificateId, ClientCertificateModel model, OffsetDateTime lastUpdated) {
        String password = encryptionUtility.encrypt(model.getKeyPassword());
        String keyContent = encryptionUtility.encrypt(model.getKeyContent());
        String certificateContent = encryptionUtility.encrypt(model.getClientCertificateContent());
        ClientCertificateKeyEntity keyEntity = new ClientCertificateKeyEntity(keyId, password, keyContent, lastUpdated);
        ClientCertificateEntity certificateEntity = new ClientCertificateEntity(certificateId, keyId, certificateContent, lastUpdated);

        return Pair.of(keyEntity, certificateEntity);
    }

    private ClientCertificateModel toModel(ClientCertificateKeyEntity certificateKeyEntity, ClientCertificateEntity certificateEntity) {
        String password = encryptionUtility.decrypt(certificateKeyEntity.getPassword());
        String keyContent = encryptionUtility.decrypt(certificateKeyEntity.getKeyContent());
        String certificateContent = encryptionUtility.decrypt(certificateEntity.getCertificateContent());
        return new ClientCertificateModel(password, keyContent, certificateContent);
    }
}
