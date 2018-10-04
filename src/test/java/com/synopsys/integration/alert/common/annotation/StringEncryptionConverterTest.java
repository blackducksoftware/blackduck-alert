package com.synopsys.integration.alert.common.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class StringEncryptionConverterTest {

    @Test
    public void testEncrypt() {
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter();
        final String expected = "test";
        final String actual = stringEncryptionConverter.convertToDatabaseColumn(expected);

        assertNotEquals(expected, actual);
    }

    @Test
    public void testDecrypt() {
        final StringEncryptionConverter stringEncryptionConverter = new StringEncryptionConverter();
        final String expected = "test";
        final String actual = stringEncryptionConverter.convertToDatabaseColumn(expected);

        assertNotEquals(expected, actual);

        final String decryptedActual = stringEncryptionConverter.convertToEntityAttribute(actual);

        assertEquals(expected, decryptedActual);
    }
}
