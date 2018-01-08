/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.web.model;

import java.util.Set;

public class NotificationRestModel extends ConfigRestModel {
    private static final long serialVersionUID = -715566918536523106L;

    private String eventKey;
    private String createdAt;
    private Set<String> notificationTypes;
    private String projectName;
    private String projectVersion;
    private Set<ComponentRestModel> components;
    private String person;
    private String projectUrl;
    private String projectVersionUrl;

    public NotificationRestModel() {
    }

    public NotificationRestModel(final String eventKey, final String createdAt, final Set<String> notificationTypes, final String projectName, final String projectVersion, final Set<ComponentRestModel> components, final String person,
            final String projectUrl, final String projectVersionUrl) {
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationTypes = notificationTypes;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.components = components;
        this.person = person;
        this.projectUrl = projectUrl;
        this.projectVersionUrl = projectVersionUrl;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Set<String> getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationTypes(final Set<String> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public Set<ComponentRestModel> getComponents() {
        return components;
    }

    public void setComponents(final Set<ComponentRestModel> components) {
        this.components = components;
    }

    public String getPerson() {
        return person;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

}
