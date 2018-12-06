package com.synopsys.integration.alert.database.relation.key;

import java.io.Serializable;

public class DescriptorFieldRelationPK implements Serializable {
    private Long descriptorId;
    private Long fieldId;

    public DescriptorFieldRelationPK() {
        // JPA requires default constructor definitions
    }

    public DescriptorFieldRelationPK(final Long descriptorId, final Long fieldId) {
        this.descriptorId = descriptorId;
        this.fieldId = fieldId;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public void setDescriptorId(final Long descriptorId) {
        this.descriptorId = descriptorId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(final Long fieldId) {
        this.fieldId = fieldId;
    }
}
