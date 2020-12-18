package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class FieldMappingField extends ConfigField {

    public FieldMappingField(String key, String label, String description) {
        super(key, label, description, FieldType.FIELD_MAPPING_INPUT);
    }
}
