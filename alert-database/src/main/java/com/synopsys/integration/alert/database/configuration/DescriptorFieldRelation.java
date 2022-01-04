/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;
import com.synopsys.integration.alert.database.configuration.key.DescriptorFieldRelationPK;

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
