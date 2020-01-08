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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "DESCRIPTOR_CONFIGS")
public class DescriptorConfigEntity extends DatabaseEntity {
    @Column(name = "DESCRIPTOR_ID")
    private Long descriptorId;
    @Column(name = "CONTEXT_ID")
    private Long contextId;
    @Column(name = "CREATED_AT")
    private Date createdAt;
    @Column(name = "LAST_UPDATED")
    private Date lastUpdated;

    @OneToMany
    @JoinColumn(name = "CONFIG_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private List<FieldValueEntity> fieldValueEntities;

    @OneToOne
    @JoinColumn(name = "ID", referencedColumnName = "CONFIG_ID", insertable = false, updatable = false)
    private ConfigGroupEntity configGroupEntity;

    public DescriptorConfigEntity() {
        // JPA requires default constructor definitions
    }

    public DescriptorConfigEntity(final Long descriptorId, final Long contextId, final Date createdAt, final Date lastUpdated) {
        this.descriptorId = descriptorId;
        this.contextId = contextId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
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

    public void setLastUpdated(final Date lastUpdated) {
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
