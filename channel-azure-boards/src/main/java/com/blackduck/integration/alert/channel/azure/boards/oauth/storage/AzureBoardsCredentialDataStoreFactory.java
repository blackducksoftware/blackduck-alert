/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.oauth.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;

@Deprecated(forRemoval = true)
@Component
public class AzureBoardsCredentialDataStoreFactory extends AbstractDataStoreFactory {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public AzureBoardsCredentialDataStoreFactory(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    /**
     * Returns a new instance of a {@link AzureBoardsCredentialDataStore} based on the given unique ID.
     *
     * <p>The {@link DataStore#getId()} must match the {@code id} parameter from this method.
     * @param id unique ID to refer to typed data store
     */
    @Override
    protected AzureBoardsCredentialDataStore createDataStore(String id) throws IOException {
        if (StoredCredential.DEFAULT_DATA_STORE_ID.equals(id)) {
            return new AzureBoardsCredentialDataStore(this, id, configurationModelConfigurationAccessor);
        }
        throw new AlertRuntimeException("This factory can only manage an azure boards credential data store");
    }

}
