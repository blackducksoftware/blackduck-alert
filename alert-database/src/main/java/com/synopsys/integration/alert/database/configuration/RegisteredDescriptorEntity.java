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
