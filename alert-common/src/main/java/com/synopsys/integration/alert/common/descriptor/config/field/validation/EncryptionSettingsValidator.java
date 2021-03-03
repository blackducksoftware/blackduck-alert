/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;

@Component
public final class EncryptionSettingsValidator extends EncryptionValidator {
    public static final String ENCRYPTION_MISSING = "Encryption configuration missing.";
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public EncryptionSettingsValidator(EncryptionUtility encryptionUtility) {
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    public ValidationResult apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        if (encryptionUtility.isInitialized()) {
            return ValidationResult.success();
        }
        return ValidationResult.errors(ENCRYPTION_MISSING);
    }

}
