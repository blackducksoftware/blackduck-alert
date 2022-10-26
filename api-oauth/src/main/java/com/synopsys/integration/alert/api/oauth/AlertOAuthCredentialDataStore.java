package com.synopsys.integration.alert.api.oauth;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStore;
import com.synopsys.integration.alert.api.oauth.database.AlertOAuthModel;
import com.synopsys.integration.alert.api.oauth.database.accessor.AlertOAuthConfigurationAccessor;

public class AlertOAuthCredentialDataStore extends AbstractDataStore<StoredCredential> {
    private final Lock lock = new ReentrantLock();
    private final AlertOAuthConfigurationAccessor accessor;

    /**
     * @param dataStoreFactory data store factory
     * @param id               data store ID
     */
    protected AlertOAuthCredentialDataStore(AlertOAuthCredentialDataStoreFactory dataStoreFactory, String id, AlertOAuthConfigurationAccessor accessor) {
        super(dataStoreFactory, id);
        this.accessor = accessor;
    }

    @Override
    public Set<String> keySet() {
        lock.lock();
        try {
            return accessor.getConfigurations().stream()
                .map(AlertOAuthModel::getId)
                .map(UUID::toString)
                .collect(Collectors.toSet());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<StoredCredential> values() {
        lock.lock();
        try {
            return accessor.getConfigurations().stream()
                .map(this::convertToStoredCredential)
                .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public StoredCredential get(String key) {
        lock.lock();
        try {
            if (null == key) {
                return null;
            }
            return retrieveCredentialMatchingKeyOrNull(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AlertOAuthCredentialDataStore set(String key, StoredCredential value) {
        lock.lock();
        try {
            if (null != key && null != value) {
                AlertOAuthModel model = convertToModel(key, value);
                UUID configurationId = UUID.fromString(key);
                Optional<AlertOAuthModel> savedModel = accessor.getConfiguration(configurationId);
                // initialize with any saved data first
                String accessToken = savedModel.flatMap(AlertOAuthModel::getAccessToken).orElse(null);
                String refreshToken = savedModel.flatMap(AlertOAuthModel::getRefreshToken).orElse(null);
                Long expirationInMillseconds = savedModel.flatMap(AlertOAuthModel::getExirationTimeMilliseconds).orElse(null);

                // update with the new credential data if present
                if (model.getAccessToken().isPresent()) {
                    accessToken = model.getAccessToken().get();
                }

                if (model.getRefreshToken().isPresent()) {
                    refreshToken = model.getRefreshToken().get();
                }

                if (model.getExirationTimeMilliseconds().isPresent()) {
                    expirationInMillseconds = model.getExirationTimeMilliseconds().get();
                }

                AlertOAuthModel updateModel = new AlertOAuthModel(configurationId, accessToken, refreshToken, expirationInMillseconds);
                accessor.updateConfiguration(model.getId(), updateModel);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public AlertOAuthCredentialDataStore clear() {
        lock.lock();
        try {
            for (String key : keySet()) {
                delete(key);
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AlertOAuthCredentialDataStore delete(String key) {
        lock.lock();
        try {
            if (null != key) {
                UUID configurationId = UUID.fromString(key);
                if (accessor.existsConfigurationById(configurationId)) {
                    accessor.deleteConfiguration(configurationId);
                }
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    private StoredCredential retrieveCredentialMatchingKeyOrNull(String configurationKey) {
        UUID configurationId = UUID.fromString(configurationKey);
        return accessor.getConfiguration(configurationId)
            .map(this::convertToStoredCredential)
            .orElse(null);
    }

    private StoredCredential convertToStoredCredential(AlertOAuthModel model) {
        StoredCredential storedCredential = new StoredCredential();
        model.getAccessToken().ifPresent(storedCredential::setAccessToken);
        model.getRefreshToken().ifPresent(storedCredential::setRefreshToken);
        model.getExirationTimeMilliseconds().ifPresent(storedCredential::setExpirationTimeMilliseconds);
        return storedCredential;
    }

    private AlertOAuthModel convertToModel(String key, StoredCredential storedCredential) {
        UUID configurationId = UUID.fromString(key);
        return new AlertOAuthModel(configurationId, storedCredential.getAccessToken(), storedCredential.getRefreshToken(), storedCredential.getExpirationTimeMilliseconds());
    }
}
