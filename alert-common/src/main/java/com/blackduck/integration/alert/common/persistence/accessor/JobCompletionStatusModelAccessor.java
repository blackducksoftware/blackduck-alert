package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedQueryDetails;

public interface JobCompletionStatusModelAccessor {
    Optional<JobCompletionStatusModel> getJobExecutionStatus(UUID jobConfigId);

    AlertPagedModel<JobCompletionStatusModel> getJobExecutionStatus(AlertPagedQueryDetails pagedQueryDetails);

    void saveExecutionStatus(JobCompletionStatusModel statusModel);
}
