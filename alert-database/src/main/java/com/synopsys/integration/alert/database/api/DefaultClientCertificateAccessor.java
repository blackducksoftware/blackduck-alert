package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ClientCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.ClientCertificateEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateRepository;

public class DefaultClientCertificateAccessor implements ClientCertificateAccessor {
    private final ClientCertificateRepository clientCertificateRepository;

    public DefaultClientCertificateAccessor(ClientCertificateRepository clientCertificateRepository) {
        this.clientCertificateRepository = clientCertificateRepository;
    }

    @Override
    public List<ClientCertificateModel> getCertificates() {
        return clientCertificateRepository.findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClientCertificateModel> getCertificate(UUID id) {
        return clientCertificateRepository.findById(id)
                .map(this::toModel);
    }

    @Override
    public ClientCertificateModel saveCertificate(ClientCertificateModel certificateModel) throws AlertConfigurationException {
        String alias = certificateModel.getAlias();
        String certificateContent = certificateModel.getCertificateContent();

        UUID id = certificateModel.getId();
        if (null == id) {
            // Mimic keystore functionality
            id = clientCertificateRepository.findByAlias(alias).map(ClientCertificateEntity::getId).orElse(null);
        } else if (!clientCertificateRepository.existsById(id)) {
            throw new AlertConfigurationException("A client certificate with that id did not exist");
        }

        ClientCertificateEntity entityToSave =
                new ClientCertificateEntity(id, alias, certificateModel.getPrivateKeyId(), certificateContent, DateUtils.createCurrentDateTimestamp());

        ClientCertificateEntity updatedEntity = clientCertificateRepository.save(entityToSave);
        return toModel(updatedEntity);
    }

    @Override
    public void deleteCertificate(String certificateAlias) {
        clientCertificateRepository.findByAlias(certificateAlias)
                .map(ClientCertificateEntity::getId)
                .ifPresent(this::deleteCertificate);
    }

    @Override
    public void deleteCertificate(UUID certificateId) {
        clientCertificateRepository.deleteById(certificateId);
    }

    private ClientCertificateModel toModel(ClientCertificateEntity entity) {
        return new ClientCertificateModel(entity.getId(), entity.getAlias(), entity.getPrivateKeyId(), entity.getCertificateContent(),
                DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }
}
