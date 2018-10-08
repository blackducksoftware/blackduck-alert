package com.synopsys.integration.alert.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AlertProperties;

public class EncryptionUtilityTest {
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_SALT = "testSalt";

    @Test
    public void testEncryption() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        Mockito.when(alertProperties.getAlertEncryptionStaticSalt()).thenReturn(Optional.of(TEST_SALT));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties);

        final String sensitiveValue = "sensitiveDataText";

        final String encryptedValue = encryptionUtility.encrypt(sensitiveValue);
        assertNotEquals(sensitiveValue, encryptedValue);
    }

    @Test
    public void testDecryption() {
        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        Mockito.when(alertProperties.getAlertEncryptionStaticSalt()).thenReturn(Optional.of(TEST_SALT));
        final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties);
        final String sensitiveValue = "sensitiveDataText";

        final String encryptedValue = encryptionUtility.encrypt(sensitiveValue);
        final String decryptedValue = encryptionUtility.decrypt(encryptedValue);
        assertEquals(sensitiveValue, decryptedValue);
    }
}
