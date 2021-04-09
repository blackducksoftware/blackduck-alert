/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Query(value = "SELECT DISTINCT jobEntity FROM DistributionJobEntity jobEntity "
                       + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobPolicyFilters policyFilters ON jobEntity.jobId = policyFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobVulnerabilitySeverityFilters vulnerabilitySeverityFilters ON jobEntity.jobId = vulnerabilitySeverityFilters.jobId "
                       + "    LEFT JOIN blackDuckDetails.blackDuckJobProjects projects ON jobEntity.jobId = projects.jobId "
                       + "    WHERE jobEntity.enabled = true"
                       + "    AND notificationTypes.notificationType IN (:notificationTypeSet)"
                       + "    AND jobEntity.distributionFrequency IN (:frequencies)"
                       + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR projects.projectName IN (:projectNames))"
                       + "    AND ("
                       + "        (vulnerabilitySeverityFilters.severityName IS NULL OR vulnerabilitySeverityFilters.severityName IN (:vulnerabilitySeverities))"
                       + "        OR (policyFilters.policyName IS NULL OR policyFilters.policyName IN (:policyNames))"
                       + "    )"
    )
    Page<DistributionJobEntity> findMatchingEnabledJobsByFilteredNotifications(
        @Param("frequencies") Collection<String> frequencies,
        @Param("notificationTypeSet") Set<String> notificationTypeSet,
        @Param("projectNames") Set<String> projectNames,
        @Param("policyNames") Set<String> policyNames,
        @Param("vulnerabilitySeverities") Set<String> vulnerabilitySeverities,
        Pageable pageable
    );

}
