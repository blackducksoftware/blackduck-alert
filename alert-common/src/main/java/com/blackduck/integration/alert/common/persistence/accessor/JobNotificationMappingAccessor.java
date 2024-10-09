package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public interface JobNotificationMappingAccessor {
    AlertPagedModel<JobToNotificationMappingModel> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize);

    Set<UUID> getUniqueJobIds(UUID correlationId);

    boolean hasJobMappings(UUID correlationId);

    void addJobMappings(List<JobToNotificationMappingModel> jobMappings);

    void removeJobMapping(UUID correlationId, UUID jobId);

    int getNotificationCountForJob(UUID correlationId, UUID jobId);

    int getCountByCorrelationId(UUID correlationId);
}
