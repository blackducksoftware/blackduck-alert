/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface JobAccessor {

    boolean hasJobsByFrequency(String frequency);

    List<DistributionJobModel> getJobsById(Collection<UUID> jobIds);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit, String searchTerm, Collection<String> descriptorsNamesToInclude);

    Optional<DistributionJobModel> getJobById(UUID jobId);

    Optional<DistributionJobModel> getJobByName(String jobName);

    DistributionJobModel createJob(DistributionJobRequestModel requestModel);

    DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertConfigurationException;

    void deleteJob(UUID jobId);

}
