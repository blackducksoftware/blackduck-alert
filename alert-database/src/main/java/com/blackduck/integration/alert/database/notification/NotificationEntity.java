/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.blackduck.integration.alert.database.BaseEntity;
import com.blackduck.integration.alert.database.DatabaseEntity;
import com.blackduck.integration.alert.database.audit.AuditNotificationRelation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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

    @Column(name = "content_id")
    private String contentId;

    @Column(name = "mapping_to_jobs")
    private boolean mappingToJobs;

    @OneToMany(mappedBy = "notificationContent")
    private final List<AuditNotificationRelation> auditNotificationRelations = new ArrayList<>();

    public NotificationEntity() {
        // JPA requires default constructor definitions
    }

    // Reserved for queries
    public NotificationEntity(
        Long id,
        OffsetDateTime createdAt,
        String provider,
        Long providerConfigId,
        OffsetDateTime providerCreationTime,
        String notificationType,
        String content,
        boolean processed,
        String contentId,
        boolean mappingToJobs
    ) {
        this.setId(id);
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
        this.processed = processed;
        this.contentId = contentId;
        this.mappingToJobs = mappingToJobs;
    }

    public NotificationEntity(
        OffsetDateTime createdAt,
        String provider,
        Long providerConfigId,
        OffsetDateTime providerCreationTime,
        String notificationType,
        String content,
        boolean processed,
        String contentId,
        boolean mappingToJobs
    ) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
        this.processed = processed;
        this.contentId = contentId;
        this.mappingToJobs = mappingToJobs;
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

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public boolean isMappingToJobs() {
        return mappingToJobs;
    }

    public void setMappingToJobsToTrue() {
        this.mappingToJobs = true;
    }
}
