/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.notification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;

@Entity
@Table(schema = "alert", name = "raw_notification_content")
public class NotificationEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.raw_notification_content_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.raw_notification_content_id_seq_generator", sequenceName = "alert.raw_notification_content_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "provider")
    private String provider;
    @Column(name = "provider_config_id")
    private Long providerConfigId;
    @Column(name = "provider_creation_time")
    private OffsetDateTime providerCreationTime;
    @Column(name = "notification_type")
    private String notificationType;
    @Column(name = "content")
    private String content;
    @Column(name = "processed")
    private boolean processed;

    @OneToMany(mappedBy = "notificationContent")
    private final List<AuditNotificationRelation> auditNotificationRelations = new ArrayList<>();

    public NotificationEntity() {
        // JPA requires default constructor definitions
    }

    // Reserved for queries
    public NotificationEntity(Long id, OffsetDateTime createdAt, String provider, Long providerConfigId, OffsetDateTime providerCreationTime, String notificationType, String content, boolean processed) {
        this.setId(id);
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
        this.processed = processed;
    }

    public NotificationEntity(OffsetDateTime createdAt, String provider, Long providerConfigId, OffsetDateTime providerCreationTime, String notificationType, String content, boolean processed) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
        this.processed = processed;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
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

    public OffsetDateTime getProviderCreationTime() {
        return providerCreationTime;
    }

    public boolean getProcessed() {
        return processed;
    }

    public void setProcessedToTrue() {
        processed = true;
    }

    public List<AuditNotificationRelation> getAuditNotificationRelations() {
        return auditNotificationRelations;
    }

}
