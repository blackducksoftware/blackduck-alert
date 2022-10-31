package com.synopsys.integration.alert.api.oauth.database.accessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.oauth.database.AlertOAuthModel;
import com.synopsys.integration.alert.api.oauth.database.configuration.AlertOAuthConfigurationEntity;
import com.synopsys.integration.alert.api.oauth.database.configuration.AlertOAuthConfigurationRepository;

@Component
public class AlertOAuthConfigurationAccessor {
    private final AlertOAuthConfigurationRepository oauthRepository;

    public AlertOAuthConfigurationAccessor(final AlertOAuthConfigurationRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }

    public Optional<AlertOAuthModel> getConfiguration(UUID id) {
        return oauthRepository.findById(id).map(this::convertToModel);
    }

    public List<AlertOAuthModel> getConfigurations() {
        return oauthRepository.findAll().stream()
            .map(this::convertToModel)
            .collect(Collectors.toList());
    }

    public boolean existsConfigurationById(UUID id) {
        return oauthRepository.existsById(id);
    }

    public AlertOAuthModel createConfiguration(AlertOAuthModel configuration) throws AlertConfigurationException {
        UUID id = configuration.getId();
        Optional<AlertOAuthConfigurationEntity> oauthEntity = oauthRepository.findById(id);
        if (oauthEntity.isPresent()) {
            throw new AlertConfigurationException(String.format("A configuration with username id '%s' is already present. Cannot create duplicate configuration.", id));
        }
        AlertOAuthConfigurationEntity entity = convertToEntity(configuration);
        entity = oauthRepository.save(entity);
        return convertToModel(entity);
    }

    public Optional<AlertOAuthModel> updateConfiguration(UUID configurationId, AlertOAuthModel configuration) {
        Optional<AlertOAuthModel> result = Optional.empty();
        boolean exists = oauthRepository.existsById(configurationId);
        if (exists) {
            AlertOAuthConfigurationEntity entity = convertToEntity(configuration);
            entity = oauthRepository.save(entity);
            result = Optional.of(convertToModel(entity));
        }
        return result;

    }

    public void deleteConfiguration(UUID configurationId) {
        oauthRepository.deleteById(configurationId);
    }

    private AlertOAuthModel convertToModel(AlertOAuthConfigurationEntity entity) {
        return new AlertOAuthModel(entity.getId(), entity.getAccessToken(), entity.getRefreshToken(), entity.getExpirationTimeMillisecondsTimeMilliseconds());
    }

    private AlertOAuthConfigurationEntity convertToEntity(AlertOAuthModel model) {
        return new AlertOAuthConfigurationEntity(
            model.getId(),
            model.getAccessToken().orElse(null),
            model.getRefreshToken().orElse(null),
            model.getExirationTimeMilliseconds().orElse(null)
        );
    }
}
