/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
