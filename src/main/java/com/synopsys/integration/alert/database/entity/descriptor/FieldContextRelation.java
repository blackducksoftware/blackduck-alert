package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Entity;

@Entity
// TODO @Table(schema = "ALERT", name = "FIELD_CONTEXTS")
public class FieldContextRelation {
    // TODO @Column(name = "FIELD_ID")
    private Long fieldId;
    // TODO @Column(name = "CONTEXT_ID")
    private Long contextId;

    public FieldContextRelation() {
        // JPA requires default constructor definitions
    }

    public FieldContextRelation(final Long fieldId, final Long contextId) {
        this.fieldId = fieldId;
        this.contextId = contextId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public Long getContextId() {
        return contextId;
    }
}
