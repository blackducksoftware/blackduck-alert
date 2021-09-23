/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;

public class DistributionWithAuditInfo extends AlertSerializableModel {
    private UUID jobId;
    private boolean enabled;
    private String jobName;
    private String channelName;
    private FrequencyType frequencyType;
    private OffsetDateTime auditTimeLastSent;
    private String auditStatus;

    public DistributionWithAuditInfo() {
    }

    public DistributionWithAuditInfo(
        UUID jobId, boolean enabled, String jobName, String channelName, FrequencyType frequencyType, OffsetDateTime auditTimeLastSent, String auditStatus
    ) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public String getJobName() {
        return jobName;
    }

    public String getChannelName() {
        return channelName;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public OffsetDateTime getAuditTimeLastSent() {
        return auditTimeLastSent;
    }

    public String getAuditStatus() {
        return auditStatus;
    }
}
