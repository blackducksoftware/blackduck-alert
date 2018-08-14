/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.alert.datasource.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blackducksoftware.integration.alert.enumeration.DigestTypeEnum;

@Entity
@Table(schema = "alert", name = "common_distribution_config")
public class CommonDistributionConfigEntity extends DatabaseEntity {
    @Column(name = "distribution_config_id")
    private Long distributionConfigId;

    @Column(name = "distribution_type")
    private String distributionType;

    @Column(name = "name")
    private String name;

    @Column(name = "frequency")
    private DigestTypeEnum frequency;

    @Column(name = "filter_by_project")
    private Boolean filterByProject;

    public CommonDistributionConfigEntity() {
    }

    public CommonDistributionConfigEntity(final Long distributionConfigId, final String distributionType, final String name, final DigestTypeEnum frequency, final Boolean filterByProject) {
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
    }

    public Long getDistributionConfigId() {
        return distributionConfigId;
    }

    public void setDistributionConfigId(final Long distributionConfigId) {
        this.distributionConfigId = distributionConfigId;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(final String distributionType) {
        this.distributionType = distributionType;
    }

    public String getName() {
        return name;
    }

    public DigestTypeEnum getFrequency() {
        return frequency;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

}
