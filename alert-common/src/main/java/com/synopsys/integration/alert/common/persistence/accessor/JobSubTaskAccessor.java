package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;

public interface JobSubTaskAccessor {

    Optional<JobSubTaskStatusModel> getSubTaskStatus(UUID parentEventId);

    JobSubTaskStatusModel createSubTaskStatus(UUID parentEventId, UUID jobId, Long remainingTaskCount, List<Long> notificationIds);

    Optional<JobSubTaskStatusModel> decrementTaskCount(UUID parentEventId);

    Optional<JobSubTaskStatusModel> removeSubTaskStatus(UUID parentEventId);

}
