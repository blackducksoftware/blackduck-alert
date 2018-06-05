package com.blackducksoftware.integration.hub.alert.web.security;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;

public class StringEncryptionConverter implements AttributeConverter<String, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        String encryptedAttribute = null;
        try {
            encryptedAttribute = PasswordEncrypter.encrypt(attribute);
        } catch (final EncryptionException e) {
            logger.error("Error encrypting attribute", e);
        }
        return encryptedAttribute;
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        String decryptedColumm = null;
        try {
            decryptedColumm = PasswordDecrypter.decrypt(dbData);
        } catch (final EncryptionException e) {
            logger.error("Error decrypting column", e);
        }
        return decryptedColumm;
    }

}
