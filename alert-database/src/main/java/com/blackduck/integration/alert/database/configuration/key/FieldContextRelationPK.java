/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.configuration.key;

import java.io.Serializable;

public class FieldContextRelationPK implements Serializable {
    private Long fieldId;
    private Long contextId;

    public FieldContextRelationPK() {
    }

    public FieldContextRelationPK(Long fieldId, Long contextId) {
        this.fieldId = fieldId;
        this.contextId = contextId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }
}
