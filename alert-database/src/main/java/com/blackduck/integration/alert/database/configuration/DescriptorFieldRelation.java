package com.blackduck.integration.alert.database.configuration;

import com.blackduck.integration.alert.database.DatabaseRelation;
import com.blackduck.integration.alert.database.configuration.key.DescriptorFieldRelationPK;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(DescriptorFieldRelationPK.class)
@Table(schema = "ALERT", name = "DESCRIPTOR_FIELDS")
public class DescriptorFieldRelation extends DatabaseRelation {
    @Id
    @Column(name = "DESCRIPTOR_ID")
    public Long descriptorId;
    @Id
    @Column(name = "FIELD_ID")
    public Long fieldId;

    public DescriptorFieldRelation() {
        // JPA requires default constructor definitions
    }

    public DescriptorFieldRelation(Long descriptorId, Long fieldId) {
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
