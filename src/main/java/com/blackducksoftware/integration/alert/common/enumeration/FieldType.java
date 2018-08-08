package com.blackducksoftware.integration.alert.common.enumeration;

public enum FieldType {
    SELECT("Select"),
    TEXT_INPUT("TextInput"),
    TEXT_AREA("TestArea"),
    PASSWORD_INPUT("PasswordInput"),
    NUMBER_INPUT("NumberInput"),
    CHECKBOX_INPUT("CheckboxInput");

    private String fieldTypeName;

    private FieldType(final String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

}
