package com.blackduck.integration.alert.api.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.alert.api.oauth.database.accessor.AlertOAuthConfigurationAccessor;

@Component
public class AlertOAuthCredentialDataStoreFactory extends AbstractDataStoreFactory {
    private final AlertOAuthConfigurationAccessor accessor;

    @Autowired
    public AlertOAuthCredentialDataStoreFactory(AlertOAuthConfigurationAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    protected AlertOAuthCredentialDataStore createDataStore(final String id) {
        if (StoredCredential.DEFAULT_DATA_STORE_ID.equals(id)) {
            return new AlertOAuthCredentialDataStore(this, id, accessor);
        }
        throw new AlertRuntimeException("This factory can only manage an azure boards credential data store");
    }
}
