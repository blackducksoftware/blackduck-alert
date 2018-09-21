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
package com.synopsys.integration.alert.database.provider.blackduck.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "blackduck_project")
public class BlackDuckProjectEntity extends DatabaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "href")
    private String href;

    @Column(name = "project_owner_email")
    private String projectOwnerEmail;

    public BlackDuckProjectEntity() {
        // JPA requires default constructor definitions
    }

    public BlackDuckProjectEntity(final String name, final String description, final String href, String projectOwnerEmail) {
        this.name = name;
        this.description = description;
        this.href = href;
        this.projectOwnerEmail = projectOwnerEmail;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHref() {
        return href;
    }

    public String getProjectOwnerEmail() {
        return projectOwnerEmail;
    }
}
