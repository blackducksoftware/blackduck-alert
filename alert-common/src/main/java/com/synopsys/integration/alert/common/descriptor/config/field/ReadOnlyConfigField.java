/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class ReadOnlyConfigField extends ConfigField {
    public ReadOnlyConfigField(String key, String label, String description) {
        super(key, label, description, FieldType.READ_ONLY);
        applyReadOnly(true);
    }

}
