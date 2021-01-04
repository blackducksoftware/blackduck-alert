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
package com.synopsys.integration.alert.database.job.blackduck;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeEntity;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectEntity;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterEntity;

@Entity
@Table(schema = "alert", name = "blackduck_job_details")
public class BlackDuckJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "global_config_id")
    private Long globalConfigId;

    @Column(name = "filter_by_project")
    private Boolean filterByProject;

    @Column(name = "project_name_pattern")
    private String projectNamePattern;

    @OneToMany
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private List<BlackDuckJobNotificationTypeEntity> blackDuckJobNotificationTypes;

    @OneToMany
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private List<BlackDuckJobPolicyFilterEntity> blackDuckJobPolicyFilters;

    @OneToMany
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private List<BlackDuckJobVulnerabilitySeverityFilterEntity> blackDuckJobVulnerabilitySeverityFilters;

    @OneToMany
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private List<BlackDuckJobProjectEntity> blackDuckJobProjects;

    public BlackDuckJobDetailsEntity() {
    }

    public BlackDuckJobDetailsEntity(UUID jobId, Long globalConfigId, Boolean filterByProject, String projectNamePattern) {
        this.jobId = jobId;
        this.globalConfigId = globalConfigId;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public Long getGlobalConfigId() {
        return globalConfigId;
    }

    public void setGlobalConfigId(Long globalConfigId) {
        this.globalConfigId = globalConfigId;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    public void setFilterByProject(Boolean filterByProject) {
        this.filterByProject = filterByProject;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public void setProjectNamePattern(String projectNamePattern) {
        this.projectNamePattern = projectNamePattern;
    }

    public List<BlackDuckJobNotificationTypeEntity> getBlackDuckJobNotificationTypes() {
        return blackDuckJobNotificationTypes;
    }

    public void setBlackDuckJobNotificationTypes(List<BlackDuckJobNotificationTypeEntity> blackDuckJobNotificationTypes) {
        this.blackDuckJobNotificationTypes = blackDuckJobNotificationTypes;
    }

    public List<BlackDuckJobPolicyFilterEntity> getBlackDuckJobPolicyFilters() {
        return blackDuckJobPolicyFilters;
    }

    public void setBlackDuckJobPolicyFilters(List<BlackDuckJobPolicyFilterEntity> blackDuckJobPolicyFilters) {
        this.blackDuckJobPolicyFilters = blackDuckJobPolicyFilters;
    }

    public List<BlackDuckJobVulnerabilitySeverityFilterEntity> getBlackDuckJobVulnerabilitySeverityFilters() {
        return blackDuckJobVulnerabilitySeverityFilters;
    }

    public void setBlackDuckJobVulnerabilitySeverityFilters(List<BlackDuckJobVulnerabilitySeverityFilterEntity> blackDuckJobVulnerabilitySeverityFilters) {
        this.blackDuckJobVulnerabilitySeverityFilters = blackDuckJobVulnerabilitySeverityFilters;
    }

    public List<BlackDuckJobProjectEntity> getBlackDuckJobProjects() {
        return blackDuckJobProjects;
    }

    public void setBlackDuckJobProjects(List<BlackDuckJobProjectEntity> blackDuckJobProjects) {
        this.blackDuckJobProjects = blackDuckJobProjects;
    }

}
