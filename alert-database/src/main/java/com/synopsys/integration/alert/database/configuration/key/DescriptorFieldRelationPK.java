/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration.key;

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
