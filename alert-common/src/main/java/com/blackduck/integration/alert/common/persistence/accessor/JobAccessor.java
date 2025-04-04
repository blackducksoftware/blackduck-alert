/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public interface JobAccessor {

    boolean hasJobsByFrequency(String frequency);

    List<DistributionJobModel> getJobsById(Collection<UUID> jobIds);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(
        int pageOffset,
        int pageLimit,
        String searchTerm,
        String sortName,
        String sortOrder,
        Collection<String> descriptorsNamesToInclude
    );

    Optional<DistributionJobModel> getJobById(UUID jobId);

    Optional<DistributionJobModel> getJobByName(String jobName);

    DistributionJobModel createJob(DistributionJobRequestModel requestModel);

    DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertConfigurationException;

    void deleteJob(UUID jobId);

}
