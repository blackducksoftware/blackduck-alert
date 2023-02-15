/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public final class ProcessedNotificationDetails extends AlertSerializableModel {
    private final UUID jobId;

    private final UUID jobExecutionId;
    private final String channelName;
    private final String jobName;

    public static ProcessedNotificationDetails fromDistributionJob(DistributionJobModel distributionJobModel) {
        return fromDistributionJob(UUID.randomUUID(), distributionJobModel);
    }

    public static ProcessedNotificationDetails fromDistributionJob(UUID jobExecutionId, DistributionJobModel distributionJobModel) {
        return new ProcessedNotificationDetails(
            jobExecutionId,
            distributionJobModel.getJobId(),
            distributionJobModel.getChannelDescriptorName(),
            distributionJobModel.getName()
        );
    }

    public ProcessedNotificationDetails(UUID jobExecutionId, UUID jobId, String channelName, String jobName) {
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.channelName = channelName;
        this.jobName = jobName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getJobName() {
        return jobName;
    }
}
