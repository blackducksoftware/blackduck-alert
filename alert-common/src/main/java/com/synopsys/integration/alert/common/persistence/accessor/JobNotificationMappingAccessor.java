package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.UUID;

import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface JobNotificationMappingAccessor {
    AlertPagedModel<JobToNotificationMappingModel> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize);

    void addJobMapping(UUID correlationId, UUID jobId, Long notificationId);

    void removeJobMapping(UUID correlationId, UUID jobId);
}
