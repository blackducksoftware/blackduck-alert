/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.configuration;

import java.util.Date;
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
    private static final long serialVersionUID = 2515539111869578867L;
    @Id
    @GeneratedValue(generator = "alert.descriptor_configs_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.descriptor_configs_id_seq", sequenceName = "alert.descriptor_configs_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "descriptor_id")
    private Long descriptorId;
    @Column(name = "context_id")
    private Long contextId;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "last_updated")
    private Date lastUpdated;

    @OneToMany
    @JoinColumn(name = "config_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<FieldValueEntity> fieldValueEntities;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "config_id", insertable = false, updatable = false)
    private ConfigGroupEntity configGroupEntity;

    public DescriptorConfigEntity() {
        // JPA requires default constructor definitions
    }

    public DescriptorConfigEntity(Long descriptorId, Long contextId, Date createdAt, Date lastUpdated) {
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<FieldValueEntity> getFieldValueEntities() {
        if (null == fieldValueEntities) {
            return List.of();
        }
        return fieldValueEntities;
    }

    public ConfigGroupEntity getConfigGroupEntity() {
        return configGroupEntity;
    }
}
