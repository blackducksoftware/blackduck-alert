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
package com.synopsys.integration.alert.database.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;

@Entity
@Table(schema = "alert", name = "raw_notification_content")
public class NotificationContent extends DatabaseEntity implements AlertNotificationWrapper {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "provider")
    private String provider;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "provider_creation_time")
    private Date providerCreationTime;
    @Column(name = "notification_type")
    private String notificationType;
    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "notificationContent")
    //@JoinColumn(name = "notification_id", referencedColumnName = "id", insertable = false, updatable = false)
    private final List<AuditNotificationRelation> auditNotificationRelations = new ArrayList<>();

    public NotificationContent() {
        // JPA requires default constructor definitions
    }

    // Reserved for queries
    public NotificationContent(final Long id, final Date createdAt, final String provider, final Date providerCreationTime, final String notificationType, final String content) {
        this.setId(id);
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
    }

    public NotificationContent(final Date createdAt, final String provider, final Date providerCreationTime, final String notificationType, final String content) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getProvider() {
        return provider;
    }

    @Override
    public String getNotificationType() {
        return notificationType;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Date getProviderCreationTime() {
        return providerCreationTime;
    }

    public List<AuditNotificationRelation> getAuditNotificationRelations() {
        return auditNotificationRelations;
    }
}
