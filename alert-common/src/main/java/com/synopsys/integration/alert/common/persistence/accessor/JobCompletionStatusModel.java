package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;

public interface JobCompletionStatusModel {
    Optional<com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel> getJobExecutionStatus(UUID jobConfigId);

    AlertPagedModel<com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel> getJobExecutionStatus(AlertPagedQueryDetails pagedQueryDetails);

    void saveExecutionStatus(com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel statusModel);
}
