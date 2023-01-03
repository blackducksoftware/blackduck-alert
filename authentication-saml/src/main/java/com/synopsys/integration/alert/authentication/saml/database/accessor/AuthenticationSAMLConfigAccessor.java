package com.synopsys.integration.alert.authentication.saml.database.accessor;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.database.configuration.AuthenticationSAMLConfigurationEntity;
import com.synopsys.integration.alert.authentication.saml.database.configuration.AuthenticationSAMLConfigurationRepository;
import com.synopsys.integration.alert.authentication.saml.model.AuthenticationSAMLConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthenticationSAMLConfigAccessor implements UniqueConfigurationAccessor<AuthenticationSAMLConfigModel> {
    private final AuthenticationSAMLConfigurationRepository authenticationSAMLConfigurationRepository;

    @Autowired
    public AuthenticationSAMLConfigAccessor(AuthenticationSAMLConfigurationRepository authenticationSAMLConfigurationRepository) {
        this.authenticationSAMLConfigurationRepository = authenticationSAMLConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationSAMLConfigModel> getConfiguration() {
        return authenticationSAMLConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesConfigurationExist() {
        return authenticationSAMLConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuthenticationSAMLConfigModel createConfiguration(AuthenticationSAMLConfigModel configuration) throws AlertConfigurationException {
        if (doesConfigurationExist()) {
            throw new AlertConfigurationException("A SAML config already exists.");
        }

        AuthenticationSAMLConfigurationEntity authenticationSAMLConfigurationEntity = toEntity(UUID.randomUUID(), configuration);
        AuthenticationSAMLConfigurationEntity savedEntity = authenticationSAMLConfigurationRepository.save(authenticationSAMLConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AuthenticationSAMLConfigModel updateConfiguration(AuthenticationSAMLConfigModel configuration) throws AlertConfigurationException {
        AuthenticationSAMLConfigurationEntity configurationEntity =
            authenticationSAMLConfigurationRepository
                .findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                .orElseThrow(() -> new AlertConfigurationException("SAML config does not exist"));

        AuthenticationSAMLConfigurationEntity authenticationSAMLConfigurationEntity = toEntity(configurationEntity.getConfigurationId(), configuration);
        AuthenticationSAMLConfigurationEntity savedEntity = authenticationSAMLConfigurationRepository.save(authenticationSAMLConfigurationEntity);

        return toModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration() {
        authenticationSAMLConfigurationRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private AuthenticationSAMLConfigModel toModel(AuthenticationSAMLConfigurationEntity authenticationSAMLConfigurationEntity) {
        return new AuthenticationSAMLConfigModel(
            authenticationSAMLConfigurationEntity.getConfigurationId().toString(),
            DateUtils.formatDate(authenticationSAMLConfigurationEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(authenticationSAMLConfigurationEntity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            authenticationSAMLConfigurationEntity.getEnabled(),
            authenticationSAMLConfigurationEntity.getForceAuth(),
            authenticationSAMLConfigurationEntity.getMetadataUrl(),
            authenticationSAMLConfigurationEntity.getMetadataFilePath(),
            authenticationSAMLConfigurationEntity.getEntityId(),
            authenticationSAMLConfigurationEntity.getEntityBaseUrl(),
            authenticationSAMLConfigurationEntity.getRequireAssertionsSigned(),
            authenticationSAMLConfigurationEntity.getRoleAttributeMapping()
        );
    }

    private AuthenticationSAMLConfigurationEntity toEntity(UUID configurationId, AuthenticationSAMLConfigModel authenticationSAMLConfigModel) {
        return new AuthenticationSAMLConfigurationEntity(
            configurationId,
            authenticationSAMLConfigModel.getEnabled().orElse(Boolean.FALSE),
            authenticationSAMLConfigModel.getForceAuth().orElse(Boolean.FALSE),
            authenticationSAMLConfigModel.getMetadataUrl().orElse(""),
            authenticationSAMLConfigModel.getMetadataFilePath().orElse(""),
            authenticationSAMLConfigModel.getEntityId().orElse(""),
            authenticationSAMLConfigModel.getEntityBaseUrl().orElse(""),
            authenticationSAMLConfigModel.getRequireAssertionsSigned().orElse(Boolean.FALSE),
            authenticationSAMLConfigModel.getRoleAttributeMapping().orElse("")
        );
    }
}
