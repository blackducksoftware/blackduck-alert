/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class CountdownConfigField extends ConfigField {
    private Long countdown;

    public CountdownConfigField(String key, String label, String description, Long countdown) {
        super(key, label, description, FieldType.COUNTDOWN);
        this.countdown = countdown;
    }

    public Long getCountdown() {
        return countdown;
    }

}
