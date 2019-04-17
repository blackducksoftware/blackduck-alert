package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class CountdownConfigField extends ConfigField {
    public Integer countdown;

    public CountdownConfigField(final String key, final String label, final String description, final String panel, final Integer countdown) {
        super(key, label, description, FieldType.COUNTDOWN.getFieldTypeName(), false, false, panel);
        this.countdown = countdown;
    }

    public static CountdownConfigField create(final String key, final String label, final String description, final Integer countdown) {
        return new CountdownConfigField(key, label, description, "", countdown);
    }
}
