/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.entity.configuration;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.BaseEntity;
import com.synopsys.integration.alert.database.relation.key.ConfigGroupEntityKey;

@Entity
@Table(schema = "ALERT", name = "CONFIG_GROUPS")
@IdClass(ConfigGroupEntityKey.class)
public class ConfigGroupEntity extends BaseEntity {
    @Column(name = "JOB_ID")
    private UUID jobId;
    @Column(name = "CONFIG_ID")
    private Long configId;

    public ConfigGroupEntity() {
        // JPA requires default constructor definitions
    }

    public ConfigGroupEntity(final UUID jobId, final Long configId) {
        this.jobId = jobId;
        this.configId = configId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getConfigId() {
        return configId;
    }
}
