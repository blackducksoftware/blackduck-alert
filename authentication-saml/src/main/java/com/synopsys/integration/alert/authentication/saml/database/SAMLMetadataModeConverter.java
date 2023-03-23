package com.synopsys.integration.alert.authentication.saml.database;

import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;

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
