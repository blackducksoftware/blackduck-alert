package com.blackduck.integration.alert.database.configuration;

import com.blackduck.integration.alert.database.DatabaseRelation;
import com.blackduck.integration.alert.database.configuration.key.FieldContextRelationPK;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(FieldContextRelationPK.class)
@Table(schema = "ALERT", name = "FIELD_CONTEXTS")
public class FieldContextRelation extends DatabaseRelation {
    @Id
    @Column(name = "FIELD_ID")
    private Long fieldId;
    @Id
    @Column(name = "CONTEXT_ID")
    private Long contextId;

    public FieldContextRelation() {
        // JPA requires default constructor definitions
    }

    public FieldContextRelation(Long fieldId, Long contextId) {
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
