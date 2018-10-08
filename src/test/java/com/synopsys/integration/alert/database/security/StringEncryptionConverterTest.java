package com.synopsys.integration.alert.database.security;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.security.EncryptionUtility;

public class StringEncryptionConverterTest {

    @Test
    public void testEncrypt() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter();
        stringEncryptionConverter.setEncryptionUtility(encryptionUtility);
        stringEncryptionConverter.convertToDatabaseColumn("test");
        Mockito.verify(encryptionUtility).encrypt(Mockito.anyString());
    }

    @Test
    public void testDecrypt() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter();
        stringEncryptionConverter.setEncryptionUtility(encryptionUtility);
        stringEncryptionConverter.convertToEntityAttribute("dbData");
        Mockito.verify(encryptionUtility).decrypt(Mockito.anyString());
    }

    @Test
    public void testEncryptEmptyString() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter();
        stringEncryptionConverter.setEncryptionUtility(encryptionUtility);
        stringEncryptionConverter.convertToDatabaseColumn("");
        Mockito.verify(encryptionUtility, Mockito.times(0)).encrypt(Mockito.anyString());
    }

    @Test
    public void testDecryptEmptyString() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter();
        stringEncryptionConverter.setEncryptionUtility(encryptionUtility);
        stringEncryptionConverter.convertToEntityAttribute("");
        Mockito.verify(encryptionUtility, Mockito.times(0)).decrypt(Mockito.anyString());
    }
}
