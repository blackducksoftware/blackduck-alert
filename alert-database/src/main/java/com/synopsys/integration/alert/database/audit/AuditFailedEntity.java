package com.synopsys.integration.alert.database.audit;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
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
    private OffsetDateTime createdAt;
    @Column(name = "job_name")
    private String jobName;

    @Column(name = "provider_key")
    private String providerKey;
    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "channel_name")
    private String channelName;
    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "error_message")
    private String errorMessage;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "error_stack_trace", length = STACK_TRACE_CHAR_LIMIT)
    private String errorStackTrace;

    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "notification_id", referencedColumnName = "notification_id", insertable = false, updatable = false)
    public AuditFailedNotificationEntity notification;

    public AuditFailedEntity() {
        // default constructor for JPA
    }

    public AuditFailedEntity(
        UUID id,
        OffsetDateTime createdAt,
        String jobName,
        String providerKey,
        String providerName,
        String channelName,
        String notificationType,
        String errorMessage,
        Long notificationId
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.jobName = jobName;
        this.providerKey = providerKey;
        this.providerName = providerName;
        this.channelName = channelName;
        this.notificationType = notificationType;
        this.errorMessage = errorMessage;
        this.notificationId = notificationId;
    }

    public AuditFailedEntity(
        UUID id,
        OffsetDateTime createdAt,
        String jobName,
        String providerKey,
        String providerName,
        String channelName,
        String notificationType,
        String errorMessage,
        String errorStackTrace,
        Long notificationId
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.jobName = jobName;
        this.providerKey = providerKey;
        this.providerName = providerName;
        this.channelName = channelName;
        this.notificationType = notificationType;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
        this.notificationId = notificationId;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getJobName() {
        return jobName;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getChannelName() {
        return channelName;
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

    public Long getNotificationId() {
        return notificationId;
    }

    public AuditFailedNotificationEntity getNotification() {
        return notification;
    }
}
