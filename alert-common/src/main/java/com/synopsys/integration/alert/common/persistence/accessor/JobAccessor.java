/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface JobAccessor {
    @Deprecated(forRemoval = true)
    List<ConfigurationJobModel> getAllJobs();

    List<ConfigurationJobModel> getMatchingEnabledJobs(String frequency, Long providerConfigId, String notificationType);

    List<ConfigurationJobModel> getMatchingEnabledJobs(Long providerConfigId, String notificationType);

    List<ConfigurationJobModel> getJobsById(Collection<UUID> jobIds);

    AlertPagedModel<ConfigurationJobModel> getPageOfJobs(int pageOffset, int pageLimit, Collection<String> descriptorsNamesToInclude);

    Optional<ConfigurationJobModel> getJobById(UUID jobId);

    Optional<ConfigurationJobModel> getJobByName(String jobName);

    List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency);

    ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteJob(UUID jobId) throws AlertDatabaseConstraintException;

}
