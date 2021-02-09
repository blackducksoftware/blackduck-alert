/**
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "ALERT", name = "CONFIG_GROUPS")
public class ConfigGroupEntity extends BaseEntity {
    private static final long serialVersionUID = -8215203462448918700L;

    @Id
    @Column(name = "CONFIG_ID")
    private Long configId;
    @Column(name = "JOB_ID")
    private UUID jobId;

    @OneToOne
    @JoinColumn(name = "CONFIG_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private DescriptorConfigEntity descriptorConfigEntity;

    public ConfigGroupEntity() {
        // JPA requires default constructor definitions
    }

    public ConfigGroupEntity(Long configId, UUID jobId) {
        this.jobId = jobId;
        this.configId = configId;
    }

    public Long getConfigId() {
        return configId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public DescriptorConfigEntity getDescriptorConfigEntity() {
        return descriptorConfigEntity;
    }
}
