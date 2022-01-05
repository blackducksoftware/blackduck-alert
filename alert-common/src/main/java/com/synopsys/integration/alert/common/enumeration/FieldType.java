/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.enumeration;

public enum FieldType {
    SELECT("Select"),
    TEXT_INPUT("TextInput"),
    TEXT_AREA("TextArea"),
    PASSWORD_INPUT("PasswordInput"),
    NUMBER_INPUT("NumberInput"),
    CHECKBOX_INPUT("CheckboxInput"),
    HIDE_CHECKBOX_INPUT("HideCheckboxInput"),
    READ_ONLY("ReadOnlyField"),
    ENDPOINT_BUTTON("EndpointButtonField"),
    ENDPOINT_SELECT("EndpointSelectField"),
    TABLE_SELECT_INPUT("TableSelectInput"),
    UPLOAD_FILE_BUTTON("UploadFileButtonField"),
    OAUTH_ENDPOINT_BUTTON("OAuthEndpointButtonField"),
    FIELD_MAPPING_INPUT("FieldMappingField");

    private final String fieldTypeName;

    FieldType(String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

    @Override
    public String toString() {
        return getFieldTypeName();
    }

}
