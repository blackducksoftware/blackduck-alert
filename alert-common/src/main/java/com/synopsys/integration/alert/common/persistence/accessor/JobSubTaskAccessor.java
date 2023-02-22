package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;

public interface JobSubTaskAccessor {

    Optional<JobSubTaskStatusModel> getSubTaskStatus(UUID id);

    JobSubTaskStatusModel createSubTaskStatus(UUID id, UUID jobId, Long remainingTaskCount, Set<Long> notificationIds);

    Optional<JobSubTaskStatusModel> updateTaskCount(UUID id, Long remainingTaskCount);

    Optional<JobSubTaskStatusModel> decrementTaskCount(UUID id);

    Optional<JobSubTaskStatusModel> removeSubTaskStatus(UUID id);

}
