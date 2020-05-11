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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;

@Entity
@Table(schema = "alert", name = "raw_notification_content")
public class NotificationEntity extends BaseEntity implements DatabaseEntity {
    private static final long serialVersionUID = -3311174366504578531L;
    @Id
    @GeneratedValue(generator = "alert.raw_notification_content_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.raw_notification_content_id_seq", sequenceName = "alert.raw_notification_content_id_seq")
    @Column(name = "id")
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "provider")
    private String provider;
    @Column(name = "provider_config_id")
    private Long providerConfigId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "provider_creation_time")
    private Date providerCreationTime;
    @Column(name = "notification_type")
    private String notificationType;
    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "notificationContent")
    private final List<AuditNotificationRelation> auditNotificationRelations = new ArrayList<>();

    public NotificationEntity() {
        // JPA requires default constructor definitions
    }

    // Reserved for queries
    public NotificationEntity(Long id, Date createdAt, String provider, Long providerConfigId, Date providerCreationTime, String notificationType, String content) {
        this.setId(id);
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
    }

    public NotificationEntity(Date createdAt, String provider, Long providerConfigId, Date providerCreationTime, String notificationType, String content) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getContent() {
        return content;
    }

    public Date getProviderCreationTime() {
        return providerCreationTime;
    }

    public List<AuditNotificationRelation> getAuditNotificationRelations() {
        return auditNotificationRelations;
    }

}
