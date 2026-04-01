/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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

    void saveExecutionDurations(JobCompletionStatusModel statusModel);
}
