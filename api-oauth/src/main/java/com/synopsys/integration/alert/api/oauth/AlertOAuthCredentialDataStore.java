package com.synopsys.integration.alert.api.oauth;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStore;

@Component
public class AlertOAuthCredentialDataStore extends AbstractDataStore<StoredCredential> {

    private final Lock lock = new ReentrantLock();

    /**
     * @param dataStoreFactory data store factory
     * @param id               data store ID
     */
    protected AlertOAuthCredentialDataStore(AlertOAuthCredentialDataStoreFactory dataStoreFactory, String id) {
        super(dataStoreFactory, id);
    }

    @Override
    public Set<String> keySet() throws IOException {
        lock.lock();
        try {
            //TODO implement
            return Set.of();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<StoredCredential> values() throws IOException {
        lock.lock();
        try {
            StoredCredential storedCredential = createStoredCredential();
            return Set.of(storedCredential);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public StoredCredential get(String key) throws IOException {
        lock.lock();
        try {
            if (null == key) {
                return null;
            }
            return retrieveCredentialMatchingEmailOrNull(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AlertOAuthCredentialDataStore set(String key, StoredCredential value) throws IOException {
        lock.lock();
        try {
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AlertOAuthCredentialDataStore clear() throws IOException {
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
            return this;
        } finally {
            lock.unlock();
        }
    }

    private StoredCredential retrieveCredentialMatchingEmailOrNull(String credentialUserEmail) {
        return createStoredCredential();
    }

    private StoredCredential createStoredCredential() {
        String accessToken = null;
        String refreshToken = null;

        String tokenExpirationMillisString = null;
        Long tokenExpirationMillis = StringUtils.isNotBlank(tokenExpirationMillisString) ? Long.parseLong(tokenExpirationMillisString) : null;

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setAccessToken(accessToken);
        storedCredential.setRefreshToken(refreshToken);
        storedCredential.setExpirationTimeMilliseconds(tokenExpirationMillis);
        return storedCredential;
    }
}
