package com.blackduck.integration.alert.api.channel.jira.distribution.custom;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JiraCustomFieldConfig extends AlertSerializableModel {
    private final String fieldName;
    private final String fieldOriginalValue;
    private @Nullable String fieldReplacementValue;

    public JiraCustomFieldConfig(String fieldName, String fieldOriginalValue) {
        this.fieldName = fieldName;
        this.fieldOriginalValue = fieldOriginalValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldOriginalValue() {
        return fieldOriginalValue;
    }

    public Optional<String> getFieldReplacementValue() {
        return Optional.ofNullable(fieldReplacementValue);
    }

    public void setFieldReplacementValue(String fieldReplacementValue) {
        this.fieldReplacementValue = StringUtils.trimToNull(fieldReplacementValue);
    }

}
