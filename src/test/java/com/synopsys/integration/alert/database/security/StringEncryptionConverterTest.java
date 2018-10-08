package com.synopsys.integration.alert.database.security;

import org.junit.Test;
import org.mockito.Mockito;

public class StringEncryptionConverterTest {
    public static final String TEST_PROPERTY_KEY = "testPropertyKey";

    @Test
    public void testEncrypt() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter() {
            @Override
            public String getPropertyKey() {
                return TEST_PROPERTY_KEY;
            }
        };
        stringEncryptionConverter.setEncryptionUtility(encryptionUtility);
        final String expected = "test";
        stringEncryptionConverter.convertToDatabaseColumn(expected);
        Mockito.verify(encryptionUtility).encrypt(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDecrypt() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter() {
            @Override
            public String getPropertyKey() {
                return TEST_PROPERTY_KEY;
            }
        };
        stringEncryptionConverter.setEncryptionUtility(encryptionUtility);
        stringEncryptionConverter.convertToEntityAttribute("test");
        Mockito.verify(encryptionUtility).decrypt(Mockito.anyString(), Mockito.anyString());
    }
}
