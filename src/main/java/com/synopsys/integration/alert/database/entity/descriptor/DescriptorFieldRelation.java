package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Entity;

import com.synopsys.integration.alert.database.relation.DatabaseRelation;

@Entity
// TODO @Table(schema = "ALERT", name = "DESCRIPTOR_FIELDS")
public class DescriptorFieldRelation extends DatabaseRelation {
    // TODO @Column(name = "DESCRIPTOR_ID")
    public Long descriptorId;
    // TODO @Column(name = "FIELD_ID")
    public Long fieldId;

    public DescriptorFieldRelation() {
        // JPA requires default constructor definitions
    }

    public DescriptorFieldRelation(final Long descriptorId, final Long fieldId) {
        this.descriptorId = descriptorId;
        this.fieldId = fieldId;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public Long getFieldId() {
        return fieldId;
    }
}
