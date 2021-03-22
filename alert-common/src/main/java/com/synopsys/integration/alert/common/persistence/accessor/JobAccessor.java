/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public interface JobAccessor {
    //TODO: Only used in tests, should be deleted
    List<DistributionJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType);

    //TODO: Only used in tests, should be deleted
    List<DistributionJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType);

    AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobs(FilteredDistributionJobRequestModel filteredDistributionJobRequestModel, int pageOffset, int pageLimit);

    List<DistributionJobModel> getJobsById(Collection<UUID> jobIds);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit, String searchTerm, Collection<String> descriptorsNamesToInclude);

    Optional<DistributionJobModel> getJobById(UUID jobId);

    Optional<DistributionJobModel> getJobByName(String jobName);

    DistributionJobModel createJob(DistributionJobRequestModel requestModel);

    DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertConfigurationException;

    void deleteJob(UUID jobId);

}
