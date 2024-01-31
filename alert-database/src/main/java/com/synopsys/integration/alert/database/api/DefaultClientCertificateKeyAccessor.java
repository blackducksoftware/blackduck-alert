package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyRepository;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DefaultClientCertificateKeyAccessor implements UniqueConfigurationAccessor<ClientCertificateKeyModel> {
    private final EncryptionUtility encryptionUtility;
    private final ClientCertificateKeyRepository clientCertificateKeyRepository;

    public DefaultClientCertificateKeyAccessor(EncryptionUtility encryptionUtility, ClientCertificateKeyRepository clientCertificateKeyRepository) {
        this.encryptionUtility = encryptionUtility;
        this.clientCertificateKeyRepository = clientCertificateKeyRepository;
    }

    @Override
    public Optional<ClientCertificateKeyModel> getConfiguration() {
        return clientCertificateKeyRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::toModel);
    }

    @Override
    public boolean doesConfigurationExist() {
        return clientCertificateKeyRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    @Override
    public ClientCertificateKeyModel createConfiguration(ClientCertificateKeyModel configuration) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException("A client certificate key already exists.");
        }

        ClientCertificateKeyEntity entityToSave = toEntity(UUID.randomUUID(), configuration, DateUtils.createCurrentDateTimestamp());
        ClientCertificateKeyEntity updatedEntity = clientCertificateKeyRepository.save(entityToSave);

        return toModel(updatedEntity);
    }

    @Override
    public ClientCertificateKeyModel updateConfiguration(ClientCertificateKeyModel configuration) throws AlertConfigurationException {
        ClientCertificateKeyEntity existingConfigurationEntity =
                clientCertificateKeyRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                        .orElseThrow(() -> new AlertConfigurationException("Client certificate key does not exist"));

        ClientCertificateKeyEntity entityToSave = toEntity(existingConfigurationEntity.getId(), configuration, DateUtils.createCurrentDateTimestamp());
        ClientCertificateKeyEntity updatedEntity = clientCertificateKeyRepository.save(entityToSave);

        return toModel(updatedEntity);
    }

    @Override
    public void deleteConfiguration() {
        clientCertificateKeyRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private ClientCertificateKeyEntity toEntity(UUID id, ClientCertificateKeyModel model, OffsetDateTime lastUpdated) {
        String password = model.getPassword().map(encryptionUtility::encrypt).orElse(null);

        return new ClientCertificateKeyEntity(id, password, model.getKeyContent(), lastUpdated);
    }

    private ClientCertificateKeyModel toModel(ClientCertificateKeyEntity entity) {
        String password = entity.getPassword();
        boolean doesPasswordExist = StringUtils.isNotBlank(password);
        if (doesPasswordExist) {
            password = encryptionUtility.decrypt(password);
        }

        return new ClientCertificateKeyModel(
                entity.getId(),
                password,
                doesPasswordExist,
                entity.getKeyContent(),
                DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }
}
