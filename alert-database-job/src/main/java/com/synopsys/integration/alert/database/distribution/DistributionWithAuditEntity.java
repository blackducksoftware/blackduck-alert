/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.distribution;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

@Entity
@Table(schema = "alert", name = "distribution_jobs")
@SecondaryTable(schema = "alert", name = "audit_entries", pkJoinColumns = @PrimaryKeyJoinColumn(name = "common_config_id"))
public class DistributionWithAuditEntity extends AlertSerializableModel {

    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "frequency_type")
    private String frequencyType;

    @Column(name = "time_last_sent", table = "audit_entries")
    private OffsetDateTime auditTimeLastSent;

    @Column(name = "status", table = "audit_entries")
    private String auditStatus;

    public DistributionWithAuditEntity() {
    }

    public DistributionWithAuditEntity(UUID jobId, Boolean enabled, String jobName, String channelName, String frequencyType, OffsetDateTime auditTimeLastSent, String auditStatus) {
        this.jobId = jobId;
        this.enabled = enabled;
        this.jobName = jobName;
        this.channelName = channelName;
        this.frequencyType = frequencyType;
        this.auditTimeLastSent = auditTimeLastSent;
        this.auditStatus = auditStatus;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public OffsetDateTime getAuditTimeLastSent() {
        return auditTimeLastSent;
    }

    public void setAuditTimeLastSent(OffsetDateTime auditTimeLastSent) {
        this.auditTimeLastSent = auditTimeLastSent;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

}
