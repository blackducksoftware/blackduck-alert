package com.blackduck.integration.alert.database.configuration.key;

import java.io.Serializable;

public class DescriptorFieldRelationPK implements Serializable {
    private Long descriptorId;
    private Long fieldId;

    public DescriptorFieldRelationPK() {
        // JPA requires default constructor definitions
    }

    public DescriptorFieldRelationPK(Long descriptorId, Long fieldId) {
        this.descriptorId = descriptorId;
        this.fieldId = fieldId;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public void setDescriptorId(Long descriptorId) {
        this.descriptorId = descriptorId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
}
