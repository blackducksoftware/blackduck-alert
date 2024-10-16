/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.custom;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class CustomFieldDefinitionModel extends AlertSerializableModel {
    private final String fieldId;
    private final String fieldType;
    private final @Nullable String fieldArrayItems;

    public CustomFieldDefinitionModel(String fieldId, String fieldType, @Nullable String fieldArrayItems) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
        this.fieldArrayItems = fieldArrayItems;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFieldArrayItems() {
        return fieldArrayItems;
    }

}
