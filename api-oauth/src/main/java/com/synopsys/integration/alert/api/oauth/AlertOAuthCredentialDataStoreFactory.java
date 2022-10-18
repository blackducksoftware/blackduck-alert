package com.synopsys.integration.alert.api.oauth;

import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;

@Component
public class AlertOAuthCredentialDataStoreFactory extends AbstractDataStoreFactory {

    public AlertOAuthCredentialDataStoreFactory() {
    }

    @Override
    protected AlertOAuthCredentialDataStore createDataStore(final String id) {
        if (StoredCredential.DEFAULT_DATA_STORE_ID.equals(id)) {
            return new AlertOAuthCredentialDataStore(this, id);
        }
        throw new AlertRuntimeException("This factory can only manage an azure boards credential data store");
    }
}
