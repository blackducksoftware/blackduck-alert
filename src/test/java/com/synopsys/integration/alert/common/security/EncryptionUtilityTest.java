package com.synopsys.integration.alert.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.FilePersistenceUtil;

public class EncryptionUtilityTest {
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_SALT = "testSalt";
    private static final String TEST_DIRECTORY = "./testDB";
    private static final String FILE_NAME = "alert_encryption_data.json";
    private AlertProperties alertProperties;
    private FilePersistenceUtil filePersistenceUtil;
    private EncryptionUtility encryptionUtility;

    @Before
    public void initializeTest() {
        alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.of(TEST_SALT));
        Mockito.when(alertProperties.getAlertConfigHome()).thenReturn(TEST_DIRECTORY);
        filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        final File file = new File(TEST_DIRECTORY, "data");
        file.mkdirs();
    }

    @After
    public void cleanupTest() throws Exception {
        if (filePersistenceUtil.exists(FILE_NAME)) {
            filePersistenceUtil.delete(FILE_NAME);
        }
    }

    @Test
    public void testEncryption() {
        final String sensitiveValue = "sensitiveDataText";
        final String encryptedValue = encryptionUtility.encrypt(sensitiveValue);
        assertNotEquals(sensitiveValue, encryptedValue);
    }

    @Test
    public void testDecryption() {
        final String sensitiveValue = "sensitiveDataText";
        final String encryptedValue = encryptionUtility.encrypt(sensitiveValue);
        final String decryptedValue = encryptionUtility.decrypt(encryptedValue);
        assertEquals(sensitiveValue, decryptedValue);
    }

    @Test
    public void testDecryptionException() {
        final String sensitiveValue = "notEncryptedHexText!";
        final String decryptedValue = encryptionUtility.decrypt(sensitiveValue);
        assertNotNull(decryptedValue);
        assertTrue(StringUtils.isBlank(decryptedValue));
    }

    @Test
    public void testDecryptionNullSaltException() {
        final String sensitiveValue = "notEncryptedHexText!";
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        final String decryptedValue = encryptionUtility.decrypt(sensitiveValue);
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
    public void testCreateEncryptionFileData() throws Exception {
        final String expectedPassword = "expectedPassword";
        final String expectedSalt = "expectedSalt";
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.empty());
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.empty());
        assertFalse(encryptionUtility.isInitialized());
        encryptionUtility.updateEncryptionFields(expectedPassword, expectedSalt);
        assertTrue(encryptionUtility.isInitialized());
        assertTrue(filePersistenceUtil.exists(FILE_NAME));
        final String content = filePersistenceUtil.readFromFile(FILE_NAME);
        assertTrue(content.contains(expectedPassword));
        assertTrue(content.contains(expectedSalt));
    }
}
