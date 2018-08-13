package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class TextAreaConfigField extends ConfigField {

    public TextAreaConfigField(final String key, final String label, final boolean required, final boolean sensitive, final FieldGroup group, final String subGroup) {
        super(key, label, FieldType.TEXT_AREA.getFieldTypeName(), required, sensitive, group, subGroup);
    }

    public TextAreaConfigField(final String key, final String label, final boolean required, final boolean sensitive) {
        super(key, label, FieldType.TEXT_AREA.getFieldTypeName(), required, sensitive);
    }

}
