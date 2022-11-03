package com.synopsys.integration.alert.api.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.api.oauth.database.accessor.AlertOAuthConfigurationAccessor;
import com.synopsys.integration.alert.api.oauth.database.accessor.MockAlertOAuthConfigurationRepository;

class AlertOAuthCredentialDataStoreFactoryTest {

    private MockAlertOAuthConfigurationRepository repository;
    private AlertOAuthConfigurationAccessor accessor;
    private AlertOAuthCredentialDataStoreFactory factory;

    @BeforeEach
    public void initAccessor() {
        repository = new MockAlertOAuthConfigurationRepository();
        accessor = new AlertOAuthConfigurationAccessor(repository);
        factory = new AlertOAuthCredentialDataStoreFactory(accessor);
    }

    @Test
    void getDataStoreForDefaultId() throws IOException {
        assertNotNull(factory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID));
    }

    @Test
    void getDataStoreForDefaultIdAfterCreation() throws IOException {
        DataStore<StoredCredential> createdDataStore = factory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
        DataStore<StoredCredential> foundDataStore = factory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
        assertNotNull(foundDataStore);
        assertEquals(createdDataStore, foundDataStore);
    }

    @Test
    void createDataStoreForDefaultId() {
        assertNotNull(factory.createDataStore(StoredCredential.DEFAULT_DATA_STORE_ID));
    }

    @Test
    void createDataStoreWithInvalidId() {
        assertThrows(AlertRuntimeException.class, () -> factory.createDataStore("invalid id"));
    }
}
