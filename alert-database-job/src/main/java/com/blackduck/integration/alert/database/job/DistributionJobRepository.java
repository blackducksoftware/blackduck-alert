/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job;

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
    boolean existsByDistributionFrequency(String distributionFrequency);

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

    //TODO: Determine if its possible to pass in a RequestModel directly into  the JPA repository methods
    @Query(value = "SELECT jobEntity FROM DistributionJobEntity jobEntity "
        + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
        + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
        + "    LEFT JOIN blackDuckDetails.blackDuckJobPolicyFilters policyFilters ON jobEntity.jobId = policyFilters.jobId "
        + "    LEFT JOIN blackDuckDetails.blackDuckJobVulnerabilitySeverityFilters vulnerabilitySeverityFilters ON jobEntity.jobId = vulnerabilitySeverityFilters.jobId "
        + "    LEFT JOIN blackDuckDetails.blackDuckJobProjects projects ON jobEntity.jobId = projects.jobId "
        + "    WHERE jobEntity.enabled = true"
        + "    AND blackDuckDetails.globalConfigId = :blackDuckConfigId"
        + "    AND notificationTypes.notificationType IN (:notificationTypeSet)"
        + "    AND jobEntity.distributionFrequency IN (:frequencies)"
        + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR blackDuckDetails.projectVersionNamePattern IS NOT NULL OR projects.projectName IN (:projectNames))"
        + "    AND ("
        + "          ("
        + "            (:#{NULL EQ #vulnerabilitySeverities} = TRUE)"
        + "            OR vulnerabilitySeverityFilters.severityName IS NULL"
        + "            OR vulnerabilitySeverityFilters.severityName IN (:vulnerabilitySeverities)"
        + "          ) AND ("
        + "            (:#{NULL EQ #policyNames} = TRUE)"
        + "            OR policyFilters.policyName IS NULL"
        + "            OR policyFilters.policyName IN (:policyNames)"
        + "          )"
        + "    )"
        + " GROUP BY jobEntity.jobId"
        + " ORDER BY jobEntity.createdAt ASC"
    )
    Page<DistributionJobEntity> findAndSortMatchingEnabledJobsByFilteredNotifications(
        @Param("blackDuckConfigId") Long blackDuckConfigId,
        @Param("frequencies") Collection<String> frequencies,
        @Param("notificationTypeSet") Set<String> notificationTypeSet,
        @Param("projectNames") Set<String> projectNames,
        @Param("policyNames") Set<String> policyNames,
        @Param("vulnerabilitySeverities") Set<String> vulnerabilitySeverities,
        Pageable pageable
    );

    @Query(value =
        "SELECT new com.blackduck.integration.alert.database.job.FilteredDistributionJob(jobEntity.jobId, notification.id, blackDuckDetails.filterByProject, blackDuckDetails.projectNamePattern, blackDuckDetails.projectVersionNamePattern)  FROM DistributionJobEntity jobEntity "
            + "    LEFT JOIN jobEntity.blackDuckJobDetails blackDuckDetails ON jobEntity.jobId = blackDuckDetails.jobId "
            + "    LEFT JOIN blackDuckDetails.blackDuckJobNotificationTypes notificationTypes ON jobEntity.jobId = notificationTypes.jobId "
            + "    LEFT JOIN blackDuckDetails.blackDuckJobPolicyFilters policyFilters ON jobEntity.jobId = policyFilters.jobId "
            + "    LEFT JOIN blackDuckDetails.blackDuckJobVulnerabilitySeverityFilters vulnerabilitySeverityFilters ON jobEntity.jobId = vulnerabilitySeverityFilters.jobId "
            + "    LEFT JOIN blackDuckDetails.blackDuckJobProjects projects ON jobEntity.jobId = projects.jobId "
            + "    LEFT JOIN NotificationEntity notification ON blackDuckDetails.globalConfigId = notification.providerConfigId "
            + "    WHERE jobEntity.enabled = true"
            + "    AND blackDuckDetails.globalConfigId = :blackDuckConfigId"
            + "    AND notification.providerConfigId = :blackDuckConfigId "
            + "    AND notification.id = :notificationId "
            + "    AND notificationTypes.notificationType IN (notification.notificationType)"
            + "    AND jobEntity.distributionFrequency IN (:frequencies)"
            + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR blackDuckDetails.projectVersionNamePattern IS NOT NULL OR projects.projectName IN (:projectNames))"
            + "    AND ("
            + "          ("
            + "            (:#{NULL EQ #vulnerabilitySeverities} = TRUE)"
            + "            OR vulnerabilitySeverityFilters.severityName IS NULL"
            + "            OR vulnerabilitySeverityFilters.severityName IN (:vulnerabilitySeverities)"
            + "          ) AND ("
            + "            (:#{NULL EQ #policyNames} = TRUE)"
            + "            OR policyFilters.policyName IS NULL"
            + "            OR policyFilters.policyName IN (:policyNames)"
            + "          )"
            + "    )"
            + " GROUP BY jobEntity.jobId, notification.id, blackDuckDetails.projectNamePattern, blackDuckDetails.projectVersionNamePattern, blackDuckDetails.filterByProject"
            + " ORDER BY jobEntity.createdAt ASC"
    )
    Page<FilteredDistributionJob> findAndSortMatchingEnabledJobsByFilteredNotification(
        @Param("notificationId") Long notificationId,
        @Param("blackDuckConfigId") Long blackDuckConfigId,
        @Param("frequencies") Collection<String> frequencies,
        @Param("projectNames") Set<String> projectNames,
        @Param("policyNames") Set<String> policyNames,
        @Param("vulnerabilitySeverities") Set<String> vulnerabilitySeverities,
        Pageable pageable
    );

    @Query(value =
        "SELECT COUNT(projects.jobId) FROM BlackDuckJobProjectEntity projects WHERE projects.jobId = :jobId AND projects.projectName IN (:projectNames)"
    )
    int countConfiguredProjectsForJob(@Param("jobId") UUID jobId, @Param("projectNames") Set<String> projectNames);

}
