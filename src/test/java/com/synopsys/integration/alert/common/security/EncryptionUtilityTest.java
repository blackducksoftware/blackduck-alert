package com.synopsys.integration.alert.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

public class EncryptionUtilityTest {
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_SALT = "testSalt";
    private static final String TEST_DIRECTORY = "./testDB";
    private static final String TEST_SECRETS_DIRECTORY = "./testDB/run/secrets";
    private static final String FILE_NAME = "alert_encryption_data.json";
    private static final String SECRETS_RELATIVE_PATH = "../run/secrets/";
    private static final String ENCRYPTION_PASSWORD_FILE = SECRETS_RELATIVE_PATH + "ALERT_ENCRYPTION_PASSWORD";
    private static final String ENCRYPTION_SALT_FILE = SECRETS_RELATIVE_PATH + "ALERT_ENCRYPTION_GLOBAL_SALT";

    private AlertProperties alertProperties;
    private FilePersistenceUtil filePersistenceUtil;
    private EncryptionUtility encryptionUtility;

    @BeforeEach
    public void initializeTest() {
        alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.of(TEST_SALT));
        Mockito.when(alertProperties.getAlertConfigHome()).thenReturn(TEST_DIRECTORY);
        Mockito.when(alertProperties.getAlertSecretsDir()).thenReturn(TEST_SECRETS_DIRECTORY);
        filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        File file = new File(TEST_DIRECTORY, "data");
        file.mkdirs();
    }

    @AfterEach
    public void cleanupTest() throws Exception {
        if (filePersistenceUtil.exists(FILE_NAME)) {
            filePersistenceUtil.delete(FILE_NAME);
        }

        if (filePersistenceUtil.exists(SECRETS_RELATIVE_PATH)) {
            filePersistenceUtil.delete(SECRETS_RELATIVE_PATH);
        }
    }

    @Test
    public void testEncryption() {
        final String sensitiveValue = "sensitiveDataText";
        String encryptedValue = encryptionUtility.encrypt(sensitiveValue);
        assertNotEquals(sensitiveValue, encryptedValue);
    }

    @Test
    public void testDecryption() {
        final String sensitiveValue = "sensitiveDataText";
        String encryptedValue = encryptionUtility.encrypt(sensitiveValue);
        String decryptedValue = encryptionUtility.decrypt(encryptedValue);
        assertEquals(sensitiveValue, decryptedValue);
    }

    @Test
    public void testDecryptionException() {
        final String sensitiveValue = "notEncryptedHexText!";
        String decryptedValue = encryptionUtility.decrypt(sensitiveValue);
        assertNotNull(decryptedValue);
        assertTrue(StringUtils.isBlank(decryptedValue));
    }

    @Test
    public void testDecryptionNullSaltException() {
        final String sensitiveValue = "notEncryptedHexText!";
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        String decryptedValue = encryptionUtility.decrypt(sensitiveValue);
        assertTrue(StringUtils.isBlank(decryptedValue));
    }

    @Test
    public void testInitializedFromEnvironment() {
        assertTrue(encryptionUtility.isInitialized());
    }

    @Test
    public void testInitializedFalsePasswordMissing() {
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        assertFalse(encryptionUtility.isInitialized());
    }

    @Test
    public void testInitializedFalseSaltMissing() {
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        assertFalse(encryptionUtility.isInitialized());
    }

    @Test
    public void testInitializedFalseBothMissing() {
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        assertFalse(encryptionUtility.isInitialized());
    }

    @Test
    public void testInitializedFromFileTrue() throws Exception {
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        filePersistenceUtil.writeToFile(FILE_NAME, "{password: \"savedPassword\", globalSalt: \"savedSalt\"}");
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(filePersistenceUtil.exists(FILE_NAME));
    }

    @Test
    public void testEncryptionFromEnvironment() {
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(encryptionUtility.isEncryptionFromEnvironment());
    }

    @Test
    public void testEncryptionFromEnvironmentFalse() throws Exception {
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        filePersistenceUtil.writeToFile(FILE_NAME, "{password: \"savedPassword\", globalSalt: \"savedSalt\"}");
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(filePersistenceUtil.exists(FILE_NAME));
        assertFalse(encryptionUtility.isEncryptionFromEnvironment());
    }

    @Test
    public void testEncryptionFromEnvironmentNoSalt() throws Exception {
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        filePersistenceUtil.writeToFile(FILE_NAME, "{password: \"savedPassword\", globalSalt: \"savedSalt\"}");
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(filePersistenceUtil.exists(FILE_NAME));
        assertTrue(encryptionUtility.isEncryptionFromEnvironment());
    }

    @Test
    public void testEncryptionFromEnvironmentNoPassword() throws Exception {
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        filePersistenceUtil.writeToFile(FILE_NAME, "{password: \"savedPassword\", globalSalt: \"savedSalt\"}");
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(filePersistenceUtil.exists(FILE_NAME));
        assertTrue(encryptionUtility.isEncryptionFromEnvironment());
    }

    @Test
    public void testCreateEncryptionFileData() throws Exception {
        final String expectedPassword = "expectedPassword";
        final String expectedSalt = "expectedSalt";
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        assertFalse(encryptionUtility.isInitialized());
        encryptionUtility.updateEncryptionFieldsInVolumeDataFile(expectedPassword, expectedSalt);
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(filePersistenceUtil.exists(FILE_NAME));
        String content = filePersistenceUtil.readFromFile(FILE_NAME);
        assertTrue(content.contains(expectedPassword));
        assertTrue(content.contains(expectedSalt));
    }

    @Test
    public void testSecretFileData() throws Exception {
        String expectedPassword = "expectedPassword";
        String expectedSalt = "expectedSalt";
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        assertFalse(encryptionUtility.isInitialized());
        filePersistenceUtil.writeToFile(ENCRYPTION_PASSWORD_FILE, expectedPassword);
        filePersistenceUtil.writeToFile(ENCRYPTION_SALT_FILE, expectedSalt);
        assertTrue(filePersistenceUtil.exists(ENCRYPTION_PASSWORD_FILE));
        assertTrue(filePersistenceUtil.exists(ENCRYPTION_SALT_FILE));
        assertTrue(encryptionUtility.isInitialized());
    }

    @Test
    public void testSecretFileDataMissing() {
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        assertFalse(filePersistenceUtil.exists(ENCRYPTION_PASSWORD_FILE));
        assertFalse(filePersistenceUtil.exists(ENCRYPTION_SALT_FILE));
        assertFalse(encryptionUtility.isInitialized());
    }

}
