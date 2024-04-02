/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration;

import java.util.List;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "config_contexts")
public class ConfigContextEntity extends BaseEntity implements DatabaseEntity {
    private static final long serialVersionUID = 782495857552722347L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "context")
    private String context;

    @OneToMany
    @JoinColumn(name = "context_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<FieldContextRelation> fieldContextRelations;

    public ConfigContextEntity() {
        // JPA requires default constructor definitions
    }

    public ConfigContextEntity(String context) {
        this.context = context;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getContext() {
        return context;
    }

    public List<FieldContextRelation> getFieldContextRelations() {
        return fieldContextRelations;
    }
}
