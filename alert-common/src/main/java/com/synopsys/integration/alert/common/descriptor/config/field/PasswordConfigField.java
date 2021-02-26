/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.EncryptionValidator;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class PasswordConfigField extends ConfigField {
    public PasswordConfigField(String key, String label, String description, EncryptionValidator encryptionValidation) {
        super(key, label, description, FieldType.PASSWORD_INPUT);
        applyValidationFunctions(encryptionValidation);
        applySensitive(true);
    }

}
