/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration;

import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "descriptor_configs")
public class DescriptorConfigEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.descriptor_configs_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.descriptor_configs_id_seq_generator", sequenceName = "alert.descriptor_configs_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "descriptor_id")
    private Long descriptorId;
    @Column(name = "context_id")
    private Long contextId;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @OneToMany
    @JoinColumn(name = "config_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<FieldValueEntity> fieldValueEntities;

    @OneToOne
    @JoinColumn(name = "descriptor_id", referencedColumnName = "id", insertable = false, updatable = false)
    private RegisteredDescriptorEntity registeredDescriptorEntity;

    public DescriptorConfigEntity() {
        // JPA requires default constructor definitions
    }

    public DescriptorConfigEntity(Long descriptorId, Long contextId, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        this.descriptorId = descriptorId;
        this.contextId = contextId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public Long getContextId() {
        return contextId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<FieldValueEntity> getFieldValueEntities() {
        if (null == fieldValueEntities) {
            return List.of();
        }
        return fieldValueEntities;
    }

}
