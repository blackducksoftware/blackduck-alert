/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.custom;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JiraCustomFieldConfig extends AlertSerializableModel {
    private final String fieldName;
    private final String fieldOriginalValue;
    private @Nullable String fieldReplacementValue;
    private boolean treatValueAsJson;

    public JiraCustomFieldConfig(String fieldName, String fieldOriginalValue) {
        this(fieldName, fieldOriginalValue, false);
    }

    public JiraCustomFieldConfig(String fieldName, String fieldOriginalValue, boolean treatValueAsJson) {
        this.fieldName = fieldName;
        this.fieldOriginalValue = fieldOriginalValue;
        this.treatValueAsJson = treatValueAsJson;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldOriginalValue() {
        return fieldOriginalValue;
    }

    public boolean isTreatValueAsJson() {
        return treatValueAsJson;
    }

    public Optional<String> getFieldReplacementValue() {
        return Optional.ofNullable(fieldReplacementValue);
    }

    public void setFieldReplacementValue(String fieldReplacementValue) {
        this.fieldReplacementValue = StringUtils.trimToNull(fieldReplacementValue);
    }

}
