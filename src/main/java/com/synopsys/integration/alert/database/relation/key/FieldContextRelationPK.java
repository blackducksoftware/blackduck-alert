package com.synopsys.integration.alert.database.relation.key;

import java.io.Serializable;

public class FieldContextRelationPK implements Serializable {
    private Long fieldId;
    private Long contextId;

    public FieldContextRelationPK() {
    }

    public FieldContextRelationPK(final Long fieldId, final Long contextId) {
        this.fieldId = fieldId;
        this.contextId = contextId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(final Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(final Long contextId) {
        this.contextId = contextId;
    }
}
