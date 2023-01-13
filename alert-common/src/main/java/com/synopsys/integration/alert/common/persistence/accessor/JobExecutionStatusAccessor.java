package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;

public interface JobExecutionStatusAccessor {
    AlertPagedModel<JobExecutionStatusModel> getJobExecutionStatus(AlertPagedQueryDetails pagedQueryDetails);

    void saveExecutionStatus(JobExecutionStatusModel statusModel);
}
