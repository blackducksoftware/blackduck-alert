package com.synopsys.integration.alert.database.api;

import static com.synopsys.integration.alert.common.rest.AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS;
import static com.synopsys.integration.alert.common.rest.AlertRestConstants.DEFAULT_CONFIGURATION_NAME;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateAndKeyModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.ClientCertificateEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyRepository;
import com.synopsys.integration.alert.database.certificates.ClientCertificateRepository;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class ClientCertificateAndKeyAccessor implements UniqueConfigurationAccessor<ClientCertificateAndKeyModel> {
    private final EncryptionUtility encryptionUtility;
    private final ClientCertificateKeyRepository keyRepository;
    private final ClientCertificateRepository certificateRepository;

    public ClientCertificateAndKeyAccessor(EncryptionUtility encryptionUtility, ClientCertificateKeyRepository keyRepository, ClientCertificateRepository certificateRepository) {
        this.encryptionUtility = encryptionUtility;
        this.keyRepository = keyRepository;
        this.certificateRepository = certificateRepository;
    }

    @Override
    public Optional<ClientCertificateAndKeyModel> getConfiguration() {
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
    public ClientCertificateAndKeyModel createConfiguration(ClientCertificateAndKeyModel configuration) throws AlertConfigurationException {
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
    public ClientCertificateAndKeyModel updateConfiguration(ClientCertificateAndKeyModel configuration) throws AlertConfigurationException {
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

    private Pair<ClientCertificateKeyEntity, ClientCertificateEntity> toEntity(UUID keyId, UUID certificateId, ClientCertificateAndKeyModel model, OffsetDateTime lastUpdated) {
        String password = encryptionUtility.encrypt(model.getKeyPassword());
        String keyContent = encryptionUtility.encrypt(model.getKeyContent());
        String certificateContent = encryptionUtility.encrypt(model.getCertificateContent());
        ClientCertificateKeyEntity keyEntity = new ClientCertificateKeyEntity(keyId, password, keyContent, lastUpdated);
        ClientCertificateEntity certificateEntity = new ClientCertificateEntity(certificateId, keyId, certificateContent, lastUpdated);

        return Pair.of(keyEntity, certificateEntity);
    }

    private ClientCertificateAndKeyModel toModel(ClientCertificateKeyEntity certificateKeyEntity, ClientCertificateEntity certificateEntity) {
        String password = encryptionUtility.decrypt(certificateKeyEntity.getPassword());
        String keyContent = encryptionUtility.decrypt(certificateKeyEntity.getKeyContent());
        String certificateContent = encryptionUtility.decrypt(certificateEntity.getCertificateContent());
        return new ClientCertificateAndKeyModel(password, keyContent, certificateContent);
    }
}
