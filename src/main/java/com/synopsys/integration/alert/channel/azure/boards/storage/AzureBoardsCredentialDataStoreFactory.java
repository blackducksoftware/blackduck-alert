package com.synopsys.integration.alert.channel.azure.boards.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannelKey;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;

@Component
public class AzureBoardsCredentialDataStoreFactory extends AbstractDataStoreFactory {
    private final AzureBoardsChannelKey azureBoardsChannelKey;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public AzureBoardsCredentialDataStoreFactory(AzureBoardsChannelKey azureBoardsChannelKey, ConfigurationAccessor configurationAccessor) {
        this.azureBoardsChannelKey = azureBoardsChannelKey;
        this.configurationAccessor = configurationAccessor;
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
            return new AzureBoardsCredentialDataStore(this, id, azureBoardsChannelKey, configurationAccessor);
        }
        throw new AlertRuntimeException("This factory can only manage an azure boards credential data store");
    }

}
