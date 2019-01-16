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
package com.synopsys.integration.alert.database.deprecated.channel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Deprecated(since = "4.0.0; removed in 6.0.0", forRemoval = true)
@Entity
@Table(schema = "alert", name = "common_distribution_config")
public class CommonDistributionConfigEntity extends DatabaseEntity {
    @Column(name = "distribution_config_id")
    private Long distributionConfigId;

    @Column(name = "distribution_type")
    private String distributionType;

    @Column(name = "name")
    private String name;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "frequency")
    private FrequencyType frequency;

    @Column(name = "filter_by_project")
    private Boolean filterByProject;

    @Column(name = "project_name_pattern")
    private String projectNamePattern;

    @Column(name = "format_type")
    private FormatType formatType;

    public CommonDistributionConfigEntity() {
        // JPA requires default constructor definitions
    }

    public CommonDistributionConfigEntity(final Long distributionConfigId, final String distributionType, final String name, final String providerName, final FrequencyType frequency, final Boolean filterByProject,
        final String projectNamePattern, final FormatType formatType) {
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.providerName = providerName;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.formatType = formatType;
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

    public String getName() {
        return name;
    }

    public String getProviderName() {
        return providerName;
    }

    public FrequencyType getFrequency() {
        return frequency;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public FormatType getFormatType() {
        return formatType;
    }

}

