/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    List<DistributionJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType);

    List<DistributionJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType);

    List<FilteredDistributionJobResponseModel> getMatchingEnabledJobs(FilteredDistributionJobRequestModel filteredDistributionJobRequestModel);

    List<DistributionJobModel> getJobsById(Collection<UUID> jobIds);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit);

    AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageOffset, int pageLimit, String searchTerm, Collection<String> descriptorsNamesToInclude);

    Optional<DistributionJobModel> getJobById(UUID jobId);

    Optional<DistributionJobModel> getJobByName(String jobName);

    DistributionJobModel createJob(DistributionJobRequestModel requestModel);

    DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertConfigurationException;

    void deleteJob(UUID jobId);

}
