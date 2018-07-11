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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(schema = "alert", name = "notification_events")
public class NotificationEntity extends DatabaseEntity {
    @Column(name = "event_key")
    private String eventKey;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "notification_type")
    private NotificationCategoryEnum notificationType;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_version")
    private String projectVersion;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "component_version")
    private String componentVersion;

    @Column(name = "policy_rule_name")
    private String policyRuleName;

    @Column(name = "policy_rule_user")
    private String policyRuleUser;

    @Column(name = "project_url")
    private String projectUrl;

    @Column(name = "project_version_url")
    private String projectVersionUrl;

    public NotificationEntity() {
    }

    public NotificationEntity(final String eventKey, final Date createdAt, final NotificationCategoryEnum notificationType, final String projectName, final String projectUrl, final String projectVersion, final String projectVersionUrl,
            final String componentName, final String componentVersion, final String policyRuleName, final String policyRuleUser) {
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.notificationType = notificationType;
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.policyRuleName = policyRuleName;
        this.policyRuleUser = policyRuleUser;
        this.projectUrl = projectUrl;
        this.projectVersionUrl = projectVersionUrl;
    }

    public String getEventKey() {
        return eventKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public NotificationCategoryEnum getNotificationType() {
        return notificationType;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getPolicyRuleName() {
        return policyRuleName;
    }

    public String getPolicyRuleUser() {
        return policyRuleUser;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }
}
