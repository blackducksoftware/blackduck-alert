/*
 * api-common-model
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.common.model.errors;

public class AlertFieldStatusMessages {
    public static final String REQUIRED_FIELD_MISSING = "Required field missing";
    public static final String INVALID_OPTION = "Invalid option selected";
    public static final String ENCRYPTION_MISSING = "Encryption configuration missing.";
    public static final String DUPLICATE_NAME_FOUND = "A configuration with this name already exists";

    private AlertFieldStatusMessages() {}
}
