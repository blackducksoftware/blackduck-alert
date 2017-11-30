/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Entity
@IdClass(HubUserProjectVersionsRelationPK.class)
@Table(name = "hub_user_project_versions")
public class HubUserProjectVersionsRelation implements Serializable {
    private static final long serialVersionUID = 4060696367381771212L;

    @Id
    @Column(name = "user_config_id")
    private Long userConfigId;

    @Id
    @Column(name = "project_name")
    private String projectName;

    @Id
    @Column(name = "project_version_name")
    private String projectVersionName;

    @Column(name = "enabled")
    private Boolean enabled;

    public HubUserProjectVersionsRelation() {
    }

    public HubUserProjectVersionsRelation(final Long userConfigId, final String projectName, final String projectVersionName, final Boolean enabled) {
        this.userConfigId = userConfigId;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.enabled = enabled;
    }

    public Long getUserConfigId() {
        return userConfigId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        return reflectionToStringBuilder.toString();
    }

}
