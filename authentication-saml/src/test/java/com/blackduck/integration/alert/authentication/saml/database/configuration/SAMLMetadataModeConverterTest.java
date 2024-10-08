package com.blackduck.integration.alert.authentication.saml.database.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.authentication.saml.database.SAMLMetadataModeConverter;
import com.blackduck.integration.alert.authentication.saml.model.SAMLMetadataMode;

class SAMLMetadataModeConverterTest {
    @Test
    void convertToDatabaseColumnReturnsIntMode() {
        SAMLMetadataModeConverter samlMetadataModeConverter = new SAMLMetadataModeConverter();

        int urlConvertedMode = samlMetadataModeConverter.convertToDatabaseColumn(SAMLMetadataMode.URL);
        assertEquals(SAMLMetadataMode.URL.getMode(), urlConvertedMode);

        int fileConvertedMode = samlMetadataModeConverter.convertToDatabaseColumn(SAMLMetadataMode.FILE);
        assertEquals(SAMLMetadataMode.FILE.getMode(), fileConvertedMode);
    }

    @Test
    void convertToEntityAttributeReturnsMetadataModeType() {
        SAMLMetadataModeConverter samlMetadataModeConverter = new SAMLMetadataModeConverter();

        assertEquals(SAMLMetadataMode.URL, samlMetadataModeConverter.convertToEntityAttribute(SAMLMetadataMode.URL.getMode()));
        assertEquals(SAMLMetadataMode.FILE, samlMetadataModeConverter.convertToEntityAttribute(SAMLMetadataMode.FILE.getMode()));
        assertNull(samlMetadataModeConverter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttributeThrowsOnInvalidIntMode() {
        SAMLMetadataModeConverter samlMetadataModeConverter = new SAMLMetadataModeConverter();

        assertThrows(IllegalArgumentException.class, () -> samlMetadataModeConverter.convertToEntityAttribute(-1));
    }
}
