/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface JobNotificationMappingAccessor {
    AlertPagedModel<JobToNotificationMappingModel> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize);

    Set<UUID> getUniqueJobIds(UUID correlationId);

    boolean hasJobMappings(UUID correlationId);

    void addJobMappings(List<JobToNotificationMappingModel> jobMappings);

    void removeJobMapping(UUID correlationId, UUID jobId);

    int getNotificationCountForJob(UUID correlationId, UUID jobId);
}
