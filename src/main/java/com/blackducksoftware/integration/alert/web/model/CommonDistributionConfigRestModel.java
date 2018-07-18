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
package com.blackducksoftware.integration.alert.web.model;

import java.util.List;

public class CommonDistributionConfigRestModel extends ConfigRestModel {
    private String distributionConfigId;
    private String distributionType;
    private String name;
    private String frequency;
    private String filterByProject;
    private List<String> configuredProjects;
    private List<String> notificationTypes;
    private String lastRan;
    private String status;

    public CommonDistributionConfigRestModel() {
    }

    public CommonDistributionConfigRestModel(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String filterByProject, final List<String> configuredProjects,
            final List<String> notificationTypes) {
        super(id);
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.configuredProjects = configuredProjects;
        this.notificationTypes = notificationTypes;
    }

    public String getDistributionConfigId() {
        return distributionConfigId;
    }

    public void setDistributionConfigId(final String distributionConfigId) {
        this.distributionConfigId = distributionConfigId;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public String getName() {
        return name;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getFilterByProject() {
        return filterByProject;
    }

    public List<String> getConfiguredProjects() {
        return configuredProjects;
    }

    public void setConfiguredProjects(final List<String> configuredProjects) {
        this.configuredProjects = configuredProjects;
    }

    public List<String> getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationTypes(final List<String> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public String getLastRan() {
        return lastRan;
    }

    public void setLastRan(final String lastRan) {
        this.lastRan = lastRan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setDistributionType(final String distributionType) {
        this.distributionType = distributionType;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFrequency(final String frequency) {
        this.frequency = frequency;
    }

    public void setFilterByProject(final String filterByProject) {
        this.filterByProject = filterByProject;
    }
}
