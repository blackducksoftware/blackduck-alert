package com.synopsys.integration.alert.api.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.oauth.database.AlertOAuthModel;
import com.synopsys.integration.alert.api.oauth.database.accessor.AlertOAuthConfigurationAccessor;
import com.synopsys.integration.alert.api.oauth.database.accessor.MockAlertOAuthConfigurationRepository;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class AlertOAuthCredentialDataStoreTest {

    private MockAlertOAuthConfigurationRepository repository;

    private AlertOAuthConfigurationAccessor accessor;
    private AlertOAuthCredentialDataStoreFactory factory;
    private AlertOAuthCredentialDataStore dataStore;

    @BeforeEach
    void initDataStore() {
        repository = new MockAlertOAuthConfigurationRepository();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        accessor = new AlertOAuthConfigurationAccessor(repository, encryptionUtility);
        factory = new AlertOAuthCredentialDataStoreFactory(accessor);
        dataStore = new AlertOAuthCredentialDataStore(factory, StoredCredential.DEFAULT_DATA_STORE_ID, accessor);
    }

    @Test
    void keySetEmpty() {
        assertTrue(dataStore.keySet().isEmpty());
    }

    @Test
    void keySetNotEmpty() throws AlertConfigurationException {
        AlertOAuthModel model1 = createTestModel();
        accessor.createConfiguration(model1);
        AlertOAuthModel model2 = createTestModel();
        accessor.createConfiguration(model2);
        AlertOAuthModel model3 = createTestModel();
        accessor.createConfiguration(model3);
        Set<String> keySet = dataStore.keySet();
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains(model1.getId().toString()));
        assertTrue(keySet.contains(model2.getId().toString()));
        assertTrue(keySet.contains(model3.getId().toString()));

    }

    @Test
    void valuesEmpty() {
        assertTrue(dataStore.keySet().isEmpty());
    }

    @Test
    void valuesNotEmpty() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        assertEquals(3, dataStore.values().size());
    }

    @Test
    void getKeyWithNullKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        assertNull(dataStore.get(null));
    }

    @Test
    void getKeyWithUnknownKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        assertNull(dataStore.get(UUID.randomUUID().toString()));
    }

    @Test
    void getKeyWithValidKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        StoredCredential storedCredential = dataStore.get(model.getId().toString());
        assertEquals(model.getAccessToken().orElse(null), storedCredential.getAccessToken());
        assertEquals(model.getRefreshToken().orElse(null), storedCredential.getRefreshToken());
        assertEquals(model.getExirationTimeMilliseconds().orElse(null), storedCredential.getExpirationTimeMilliseconds());
    }

    @Test
    void deleteWithNullKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        dataStore.delete(null);
        assertEquals(3, repository.count());
    }

    @Test
    void deleteWithUnknownKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        dataStore.delete(UUID.randomUUID().toString());
        assertEquals(3, repository.count());
    }

    @Test
    void deleteWithValidKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        dataStore.delete(model.getId().toString());
        assertEquals(2, repository.count());

    }

    @Test
    void clearExistingKeys() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        model = createTestModel();
        accessor.createConfiguration(model);
        dataStore.clear();
        assertEquals(0, repository.count());
    }

    @Test
    void setWithNullKey() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        StoredCredential storedCredential = new StoredCredential();
        dataStore.set(null, storedCredential);
        assertEquals(model, accessor.getConfiguration(model.getId()).orElseThrow(() -> new AssertionError("created model not found.")));
    }

    @Test
    void setWithNullValue() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        dataStore.set(model.getId().toString(), null);
        assertEquals(model, accessor.getConfiguration(model.getId()).orElseThrow(() -> new AssertionError("created model not found.")));
    }

    @Test
    void setWithNullKeyAndValue() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        dataStore.set(null, null);
        assertEquals(model, accessor.getConfiguration(model.getId()).orElseThrow(() -> new AssertionError("created model not found.")));
    }

    @Test
    void setWithUpdatedValues() throws AlertConfigurationException {
        AlertOAuthModel model = createTestModel();
        accessor.createConfiguration(model);
        StoredCredential updatedCredential = new StoredCredential();
        updatedCredential.setAccessToken("updatedAccessToken");
        updatedCredential.setRefreshToken("updatedRefreshToken");
        updatedCredential.setExpirationTimeMilliseconds(100L);
        dataStore.set(model.getId().toString(), updatedCredential);

        StoredCredential savedCredential = dataStore.get(model.getId().toString());
        assertEquals(updatedCredential, savedCredential);

    }

    private AlertOAuthModel createTestModel() {
        UUID id = UUID.randomUUID();
        String accessToken = "accessToken" + id;
        String refreshToken = "refreshToken" + id;
        Long expirationTime = 5000L;
        return new AlertOAuthModel(id, accessToken, refreshToken, expirationTime);
    }
}
