/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.configuration;

import java.util.List;

import com.blackduck.integration.alert.database.BaseEntity;
import com.blackduck.integration.alert.database.DatabaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "registered_descriptors")
public class RegisteredDescriptorEntity extends BaseEntity implements DatabaseEntity {
    private static final long serialVersionUID = -7695067320131039823L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;

    @Column(name = "type_id")
    private Long typeId;

    @OneToMany
    @JoinColumn(name = "descriptor_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<DescriptorConfigEntity> descriptorConfigEntities;

    public RegisteredDescriptorEntity() {
        // JPA requires default constructor definitions
    }

    public RegisteredDescriptorEntity(String name, Long typeId) {
        this.name = name;
        this.typeId = typeId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Long getTypeId() {
        return typeId;
    }

    public List<DescriptorConfigEntity> getDescriptorConfigEntities() {
        return descriptorConfigEntities;
    }
}
