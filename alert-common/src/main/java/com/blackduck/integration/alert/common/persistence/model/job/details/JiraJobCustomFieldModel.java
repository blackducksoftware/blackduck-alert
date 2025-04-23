/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JiraJobCustomFieldModel extends AlertSerializableModel {
    private String fieldName;
    private String fieldValue;
    private boolean createJsonObject;

    public JiraJobCustomFieldModel() {
    }

    public JiraJobCustomFieldModel(String fieldName, String fieldValue) {
        this(fieldName, fieldValue, false);
    }

    public JiraJobCustomFieldModel(String fieldName, String fieldValue, boolean createJsonObject) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.createJsonObject = createJsonObject;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public boolean isCreateJsonObject() {
        return createJsonObject;
    }

}
