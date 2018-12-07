package com.synopsys.integration.alert.database.entity.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.relation.key.FieldContextRelationPK;

@Entity
@IdClass(FieldContextRelationPK.class)
@Table(schema = "ALERT", name = "FIELD_CONTEXTS")
public class FieldContextRelation {
    @Id
    @Column(name = "FIELD_ID")
    private Long fieldId;
    @Id
    @Column(name = "CONTEXT_ID")
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
