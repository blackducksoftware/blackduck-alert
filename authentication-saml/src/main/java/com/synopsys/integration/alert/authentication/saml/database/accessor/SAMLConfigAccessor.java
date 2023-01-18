package com.synopsys.integration.alert.authentication.saml.database.accessor;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.database.configuration.SAMLConfigurationEntity;
import com.synopsys.integration.alert.authentication.saml.database.configuration.SAMLConfigurationRepository;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class SAMLConfigAccessor implements UniqueConfigurationAccessor<SAMLConfigModel> {
    private final SAMLConfigurationRepository samlConfigurationRepository;

    @Autowired
    public SAMLConfigAccessor(SAMLConfigurationRepository samlConfigurationRepository) {
        this.samlConfigurationRepository = samlConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SAMLConfigModel> getConfiguration() {
        return samlConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesConfigurationExist() {
        return samlConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SAMLConfigModel createConfiguration(SAMLConfigModel configuration) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException("A SAML config already exists.");
        }
        OffsetDateTime createAndUpdatedDateTime = DateUtils.createCurrentDateTimestamp();
        SAMLConfigurationEntity samlConfigurationEntity = toEntity(UUID.randomUUID(), configuration, createAndUpdatedDateTime, createAndUpdatedDateTime);
        SAMLConfigurationEntity savedEntity = samlConfigurationRepository.save(samlConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SAMLConfigModel updateConfiguration(SAMLConfigModel configuration) throws AlertConfigurationException {
        SAMLConfigurationEntity configurationEntity =
            samlConfigurationRepository
                .findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                .orElseThrow(() -> new AlertConfigurationException("SAML config does not exist"));

        SAMLConfigurationEntity samlConfigurationEntity = toEntity(configurationEntity.getConfigurationId(), configuration, configurationEntity.getCreatedAt(), DateUtils.createCurrentDateTimestamp());
        SAMLConfigurationEntity savedEntity = samlConfigurationRepository.save(samlConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration() {
        samlConfigurationRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private SAMLConfigModel toModel(SAMLConfigurationEntity samlConfigurationEntity) {
        return new SAMLConfigModel(
            samlConfigurationEntity.getConfigurationId().toString(),
            DateUtils.formatDate(samlConfigurationEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(samlConfigurationEntity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            samlConfigurationEntity.getEnabled(),
            samlConfigurationEntity.getForceAuth(),
            samlConfigurationEntity.getMetadataUrl(),
            samlConfigurationEntity.getMetadataFilePath(),
            samlConfigurationEntity.getEntityId(),
            samlConfigurationEntity.getEntityBaseUrl(),
            samlConfigurationEntity.getRequireAssertionsSigned(),
            samlConfigurationEntity.getRoleAttributeMapping()
        );
    }

    private SAMLConfigurationEntity toEntity(UUID configurationId, SAMLConfigModel samlConfigModel, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        return new SAMLConfigurationEntity(
            configurationId,
            createdTime,
            lastUpdated,
            samlConfigModel.getEnabled().orElse(Boolean.FALSE),
            samlConfigModel.getForceAuth().orElse(Boolean.FALSE),
            samlConfigModel.getMetadataUrl().orElse(""),
            samlConfigModel.getMetadataFilePath().orElse(""),
            samlConfigModel.getEntityId(),
            samlConfigModel.getEntityBaseUrl(),
            samlConfigModel.getRequireAssertionsSigned().orElse(Boolean.FALSE),
            samlConfigModel.getRoleAttributeMapping().orElse("")
        );
    }
}
