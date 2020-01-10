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
package com.synopsys.integration.alert.database.provider.polaris.issue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "polaris_issues")
public class PolarisIssueEntity extends DatabaseEntity {
    @Column(name = "issue_type")
    private String issueType;
    @Column(name = "previous_count")
    private Integer previousCount;
    @Column(name = "current_count")
    private Integer currentCount;
    @Column(name = "project_id")
    private Long projectId;

    public PolarisIssueEntity() {
        // JPA requires default constructor definitions
    }

    public PolarisIssueEntity(final String issueType, final Integer previousCount, final Integer currentCount, final Long projectId) {
        this.issueType = issueType;
        this.previousCount = previousCount;
        this.currentCount = currentCount;
        this.projectId = projectId;
    }

    public String getIssueType() {
        return issueType;
    }

    public Integer getPreviousCount() {
        return previousCount;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public Long getProjectId() {
        return projectId;
    }
}
