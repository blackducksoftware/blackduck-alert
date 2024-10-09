package com.blackduck.integration.alert.authentication.saml.database;

import java.util.stream.Stream;

import com.blackduck.integration.alert.authentication.saml.model.SAMLMetadataMode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SAMLMetadataModeConverter implements AttributeConverter<SAMLMetadataMode, Integer> {
    @Override
    public Integer convertToDatabaseColumn(SAMLMetadataMode samlMetadataMode) {
        if (samlMetadataMode == null) {
            return null;
        }
        return samlMetadataMode.getMode();
    }

    @Override
    public SAMLMetadataMode convertToEntityAttribute(Integer mode) {
        if (mode == null) {
            return null;
        }

        return Stream.of(SAMLMetadataMode.values())
            .filter(metadataMode -> metadataMode.getMode().equals(mode))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
