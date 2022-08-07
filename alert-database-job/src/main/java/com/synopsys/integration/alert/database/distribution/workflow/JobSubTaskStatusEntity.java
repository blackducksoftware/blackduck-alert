package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "job_sub_task_status")
public class JobSubTaskStatusEntity extends BaseEntity {
    private static final long serialVersionUID = -6039296175355842298L;
    @Id
    @Column(name = "parent_event_id")
    private UUID parentEventId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "remaining_event_count")
    private Long remainingEvents;

    @Column(name = "audit_correlation_id")
    private UUID auditCorrelationId;

    @OneToMany
    @JoinColumn(name = "audit_correlation_id", referencedColumnName = "audit_correlation_id", insertable = false, updatable = false)
    private List<AuditCorrelationToNotificationRelation> auditCorrelationToNotificationRelationList;

    public JobSubTaskStatusEntity() {
    }

    public JobSubTaskStatusEntity(UUID parentEventId, UUID jobId, Long remainingEvents, UUID auditCorrelationId) {
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.remainingEvents = remainingEvents;
        this.auditCorrelationId = auditCorrelationId;
    }

    public UUID getParentEventId() {
        return parentEventId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getRemainingEvents() {
        return remainingEvents;
    }

    public UUID getAuditCorrelationId() {
        return auditCorrelationId;
    }

    public List<AuditCorrelationToNotificationRelation> getAuditCorrelationToNotificationRelationList() {
        return auditCorrelationToNotificationRelationList;
    }
}
