/**
 * alert-database
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
package com.synopsys.integration.alert.database.job;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DistributionJobRepository extends JpaRepository<DistributionJobEntity, UUID> {
    Optional<DistributionJobEntity> findByName(String name);

    List<DistributionJobEntity> findByDistributionFrequency(String distributionFrequency);

    Page<DistributionJobEntity> findByChannelDescriptorNameIn(Collection<String> channelDescriptorName, Pageable pageable);

    @Query("SELECT jobEntity FROM DistributionJobEntity jobEntity"
               + " WHERE jobEntity.channelDescriptorName IN (:channelDescriptorNames)"
               + " AND ("
               + "   jobEntity.name LIKE %:searchTerm%"
               + "   OR jobEntity.channelDescriptorName LIKE %:searchTerm%"
               + "   OR jobEntity.distributionFrequency LIKE %:searchTerm%"
               + "   OR COALESCE(to_char(jobEntity.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%"
               + "   OR (jobEntity.lastUpdated != NULL AND COALESCE(to_char(jobEntity.lastUpdated, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%)"
               + " )"
    )
    Page<DistributionJobEntity> findByChannelDescriptorNamesAndSearchTerm(@Param("channelDescriptorNames") Collection<String> channelDescriptorNames, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(value = "SELECT jobEntity FROM DistributionJobEntity jobEntity "
                       + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
                       + "    WHERE jobEntity.enabled = true"
                       + "    AND jobEntity.distributionFrequency = :frequency"
                       + "    AND blackDuckDetails.globalConfigId = :providerConfigId"
                       + "    AND notificationTypes.notificationType = :notificationType"
    )
    List<DistributionJobEntity> findMatchingEnabledJob(@Param("frequency") String frequency, @Param("providerConfigId") Long providerConfigId, @Param("notificationType") String notificationType);

    @Query(value = "SELECT jobEntity FROM DistributionJobEntity jobEntity "
                       + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
                       + "    WHERE jobEntity.enabled = true"
                       + "    AND blackDuckDetails.globalConfigId = :providerConfigId"
                       + "    AND notificationTypes.notificationType = :notificationType"
    )
    List<DistributionJobEntity> findMatchingEnabledJob(@Param("providerConfigId") Long providerConfigId, @Param("notificationType") String notificationType);

    @Query(value = "SELECT DISTINCT jobEntity FROM DistributionJobEntity jobEntity "
                       + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobPolicyFilters policyFilters ON jobEntity.jobId = policyFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobVulnerabilitySeverityFilters vulnerabilitySeverityFilters ON jobEntity.jobId = vulnerabilitySeverityFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobProjects projects ON jobEntity.jobId = projects.jobId "
                       + "    WHERE jobEntity.enabled = true"
                       + "    AND notificationTypes.notificationType = :notificationType"
                       + "    AND jobEntity.distributionFrequency IN (:frequencies)"
                       + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR projects.projectName = :projectName)"
    )
    List<DistributionJobEntity> findMatchingEnabledJobs(
        @Param("frequencies") Collection<String> frequencies,
        @Param("notificationType") String notificationType,
        @Param("projectName") String projectName
    );

    @Query(value = "SELECT DISTINCT jobEntity FROM DistributionJobEntity jobEntity "
                       + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobPolicyFilters policyFilters ON jobEntity.jobId = policyFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobVulnerabilitySeverityFilters vulnerabilitySeverityFilters ON jobEntity.jobId = vulnerabilitySeverityFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobProjects projects ON jobEntity.jobId = projects.jobId "
                       + "    WHERE jobEntity.enabled = true"
                       + "    AND notificationTypes.notificationType = :notificationType"
                       + "    AND jobEntity.distributionFrequency IN (:frequencies)"
                       + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR projects.projectName = :projectName)"
                       + "    AND (policyFilters.policyName IS NULL OR policyFilters.policyName IN (:policyNames))"
    )
    List<DistributionJobEntity> findMatchingEnabledJobsWithPolicyNames(
        @Param("frequencies") Collection<String> frequencies,
        @Param("notificationType") String notificationType,
        @Param("projectName") String projectName,
        @Param("policyNames") Collection<String> policyNames
    );

    // FIXME these queries can be improved by using pattern searching in the query and returning a FilteredDistributionJobResponseModel
    @Query(value = "SELECT DISTINCT jobEntity FROM DistributionJobEntity jobEntity "
                       + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobPolicyFilters policyFilters ON jobEntity.jobId = policyFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobVulnerabilitySeverityFilters vulnerabilitySeverityFilters ON jobEntity.jobId = vulnerabilitySeverityFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobProjects projects ON jobEntity.jobId = projects.jobId "
                       + "    WHERE jobEntity.enabled = true"
                       + "    AND notificationTypes.notificationType = :notificationType"
                       + "    AND jobEntity.distributionFrequency IN (:frequencies)"
                       + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR projects.projectName = :projectName)"
                       + "    AND (vulnerabilitySeverityFilters.severityName IS NULL OR vulnerabilitySeverityFilters.severityName IN (:vulnerabilitySeverities))"
    )
    List<DistributionJobEntity> findMatchingEnabledJobsWithVulnerabilitySeverities(
        @Param("frequencies") Collection<String> frequencies,
        @Param("notificationType") String notificationType,
        @Param("projectName") String projectName,
        @Param("vulnerabilitySeverities") Collection<String> vulnerabilitySeverities
    );
}
