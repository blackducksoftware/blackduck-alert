/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "defined_fields")
public class DefinedFieldEntity extends BaseEntity implements DatabaseEntity {
    private static final long serialVersionUID = -3477745434187375522L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "source_key")
    private String key;
    @Column(name = "sensitive")
    private Boolean sensitive;

    @OneToMany
    @JoinColumn(name = "field_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<DescriptorFieldRelation> descriptorFieldRelations;

    @OneToMany
    @JoinColumn(name = "field_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<FieldContextRelation> fieldContextRelations;

    public DefinedFieldEntity() {
        // JPA requires default constructor definitions
    }

    public DefinedFieldEntity(String key, Boolean sensitive) {
        this.key = key;
        this.sensitive = sensitive;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public Boolean getSensitive() {
        return sensitive;
    }

    public List<DescriptorFieldRelation> getDescriptorFieldRelations() {
        return descriptorFieldRelations;
    }

    public List<FieldContextRelation> getFieldContextRelations() {
        return fieldContextRelations;
    }
}
