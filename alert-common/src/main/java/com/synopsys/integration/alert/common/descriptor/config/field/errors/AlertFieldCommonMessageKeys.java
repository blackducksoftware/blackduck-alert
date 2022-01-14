/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.errors;

public enum AlertFieldCommonMessageKeys {

    ENCRYPTION_MISSING_KEY("Encryption configuration missing."),
    INVALID_OPTION_KEY("Invalid option selected"),
    REQUIRED_FIELD_MISSING_KEY("Required field missing");

    private final String message;

    AlertFieldCommonMessageKeys(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
