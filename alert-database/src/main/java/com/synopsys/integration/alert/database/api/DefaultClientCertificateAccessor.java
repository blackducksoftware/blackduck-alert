package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.ClientCertificateEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateRepository;

public class DefaultClientCertificateAccessor implements UniqueConfigurationAccessor<ClientCertificateModel> {
    private final ClientCertificateRepository clientCertificateRepository;

    public DefaultClientCertificateAccessor(ClientCertificateRepository clientCertificateRepository) {
        this.clientCertificateRepository = clientCertificateRepository;
    }

    @Override
    public Optional<ClientCertificateModel> getConfiguration() {
        return clientCertificateRepository.findByAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS).map(this::toModel);
    }

    @Override
    public boolean doesConfigurationExist() {
        return clientCertificateRepository.existsByAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
    }

    @Override
    public ClientCertificateModel createConfiguration(ClientCertificateModel configuration) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException("A client certificate already exists.");
        }

        ClientCertificateEntity configurationEntity =
                toEntity(UUID.randomUUID(), configuration.getPrivateKeyId(), configuration, DateUtils.createCurrentDateTimestamp());

        return toModel(clientCertificateRepository.save(configurationEntity));
    }

    @Override
    public ClientCertificateModel updateConfiguration(ClientCertificateModel configuration) throws AlertConfigurationException {
        ClientCertificateEntity existingConfigurationEntity = clientCertificateRepository.findByAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS)
                .orElseThrow(() -> new AlertConfigurationException("Client certificate does not exist"));

        // Use existing config id and private key id
        ClientCertificateEntity entityToSave = toEntity(
                existingConfigurationEntity.getId(),
                existingConfigurationEntity.getPrivateKeyId(),
                configuration,
                DateUtils.createCurrentDateTimestamp());
        ClientCertificateEntity updatedEntity = clientCertificateRepository.save(entityToSave);

        return toModel(updatedEntity);
    }

    @Override
    public void deleteConfiguration() {
        clientCertificateRepository.deleteByAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
    }

    private ClientCertificateModel toModel(ClientCertificateEntity entity) {
        return new ClientCertificateModel(
                entity.getId(),
                AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS,
                entity.getPrivateKeyId(),
                entity.getCertificateContent(),
                DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }

    private ClientCertificateEntity toEntity(UUID id, UUID privateKeyId, ClientCertificateModel model, OffsetDateTime lastUpdated) {
        return new ClientCertificateEntity(id, AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS, privateKeyId, model.getCertificateContent(), lastUpdated);
    };
}
