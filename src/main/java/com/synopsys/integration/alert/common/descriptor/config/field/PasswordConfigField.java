package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class PasswordConfigField extends ConfigField {

    public PasswordConfigField(final String key, final String label, final boolean required, final FieldGroup group, final String subGroup) {
        super(key, label, FieldType.PASSWORD_INPUT.getFieldTypeName(), required, true, group, subGroup);
    }

    public PasswordConfigField(final String key, final String label, final boolean required) {
        super(key, label, FieldType.PASSWORD_INPUT.getFieldTypeName(), required, true);
    }

}
