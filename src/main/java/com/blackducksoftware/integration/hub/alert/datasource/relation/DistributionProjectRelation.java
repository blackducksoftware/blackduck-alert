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
package com.blackducksoftware.integration.hub.alert.datasource.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.blackducksoftware.integration.hub.alert.datasource.relation.key.DistributionProjectRelationPK;

@Entity
@IdClass(DistributionProjectRelationPK.class)
@Table(schema = "alert", name = "distribution_project_relation")
public class DistributionProjectRelation extends DatabaseRelation {
    @Id
    @Column(name = "common_distribution_config_id")
    private Long commonDistributionConfigId;

    @Id
    @Column(name = "project_id")
    private Long projectId;

    public DistributionProjectRelation() {
    }

    public DistributionProjectRelation(final Long commonDistributionConfigId, final Long projectId) {
        super();
        this.commonDistributionConfigId = commonDistributionConfigId;
        this.projectId = projectId;
    }

    public Long getCommonDistributionConfigId() {
        return commonDistributionConfigId;
    }

    public Long getProjectId() {
        return projectId;
    }

}
