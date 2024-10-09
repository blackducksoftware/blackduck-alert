package com.blackduck.integration.alert.common.persistence.model.job.details;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JiraJobCustomFieldModel extends AlertSerializableModel {
    private String fieldName;
    private String fieldValue;

    public JiraJobCustomFieldModel() {
    }

    public JiraJobCustomFieldModel(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

}
