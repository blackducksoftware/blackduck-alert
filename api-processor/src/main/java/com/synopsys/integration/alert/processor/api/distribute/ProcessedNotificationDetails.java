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
    private final String channelName;
    private final String jobName;

    public static ProcessedNotificationDetails fromDistributionJob(DistributionJobModel distributionJobModel) {
        return new ProcessedNotificationDetails(
            distributionJobModel.getJobId(),
            distributionJobModel.getChannelDescriptorName(),
            distributionJobModel.getName()
        );
    }

    public ProcessedNotificationDetails(UUID jobId, String channelName, String jobName) {
        this.jobId = jobId;
        this.channelName = channelName;
        this.jobName = jobName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getJobName() {
        return jobName;
    }
}
