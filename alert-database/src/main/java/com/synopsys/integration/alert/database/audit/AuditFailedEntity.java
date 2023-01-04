package com.synopsys.integration.alert.database.audit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "failed_audit_entries")
public class AuditFailedEntity extends BaseEntity {
    private static final long serialVersionUID = -5935694084533618338L;
    public static final int STACK_TRACE_CHAR_LIMIT = 10000;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "time_created")
    private OffsetDateTime timeCreated;
    @Column(name = "job_config_id")
    private UUID jobConfigId;
    @Column(name = "provider_id")
    private Long providerId;
    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "error_message")
    private String errorMessage;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "error_stack_trace", length = STACK_TRACE_CHAR_LIMIT)
    private String errorStackTrace;

    @OneToMany(mappedBy = "auditFailedEntity")
    private List<AuditFailedNotificationRelation> auditFailedNotificationRelations;

    public AuditFailedEntity() {
        // default constructor for JPA
    }

    public AuditFailedEntity(
        UUID id,
        OffsetDateTime timeCreated,
        UUID jobConfigId,
        Long providerId,
        String notificationType,
        String errorMessage
    ) {
        this.id = id;
        this.timeCreated = timeCreated;
        this.jobConfigId = jobConfigId;
        this.providerId = providerId;
        this.notificationType = notificationType;
        this.errorMessage = errorMessage;
    }

    public AuditFailedEntity(
        UUID id,
        OffsetDateTime timeCreated,
        UUID jobConfigId,
        Long providerId,
        String notificationType,
        String errorMessage,
        String errorStackTrace
    ) {
        this.id = id;
        this.timeCreated = timeCreated;
        this.jobConfigId = jobConfigId;
        this.providerId = providerId;
        this.notificationType = notificationType;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Optional<String> getErrorStackTrace() {
        return Optional.ofNullable(errorStackTrace);
    }
}
