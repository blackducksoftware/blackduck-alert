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
                       + "    AND (blackDuckDetails.filterByProject = false OR blackDuckDetails.projectNamePattern IS NOT NULL OR projects.projectName IN (:projectNames))"
                       + "    AND ("
                       + "          ("
                       + "            coalesce(:vulnerabilitySeverities, NULL) IS NULL"
                       + "            OR vulnerabilitySeverityFilters.severityName IS NULL"
                       + "            OR vulnerabilitySeverityFilters.severityName IN (:vulnerabilitySeverities)"
                       + "          ) OR ("
                       + "            coalesce(:policyNames, NULL) IS NULL"
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

    String QUERY_FROM_ENABLED_JOBS_MATCHING_FILTERS =
        "FROM alert.distribution_jobs job_entity"
            + "    LEFT OUTER JOIN alert.blackduck_job_details bd_details ON job_entity.job_id = bd_details.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_notification_types notif_type ON job_entity.job_id = notif_type.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_policy_filters policy_filter ON job_entity.job_id = policy_filter.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_vulnerability_severity_filters vuln_filter ON job_entity.job_id = vuln_filter.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_projects projects ON job_entity.job_id = projects.job_id"
            + "    WHERE job_entity.enabled = true"
            + "    AND bd_details.global_config_id = :blackDuckConfigId"
            + "    AND notif_type.notification_type IN (:notificationTypeSet)"
            + "    AND job_entity.distribution_frequency IN (:frequencies)"
            + "    AND (bd_details.filter_by_project = false OR bd_details.project_name_pattern IS NOT NULL OR projects.project_name IN (:projectNames))"
            + "    AND ("
            + "          ("
            + "            coalesce(:vulnerabilitySeverities, NULL) IS NULL"
            + "            OR vuln_filter.severity_name IS NULL"
            + "            OR vuln_filter.severity_name IN (:vulnerabilitySeverities)"
            + "          ) OR ("
            + "            coalesce(:policyNames, NULL) IS NULL"
            + "            OR policy_filter.policy_name IS NULL"
            + "            OR policy_filter.policy_name IN (:policyNames)"
            + "          )"
            + "    )"
            + " GROUP BY job_entity.job_id"
            + " ORDER BY job_entity.created_at ASC";

    @Query(nativeQuery = true,
        value = "SELECT job_entity.job_id, job_entity.name, job_entity.enabled, job_entity.distribution_frequency, job_entity.processing_type, job_entity.channel_descriptor_name, job_entity.created_at, job_entity.last_updated "
                    + QUERY_FROM_ENABLED_JOBS_MATCHING_FILTERS,
        countQuery = "SELECT count(job_entity.job_id) " + QUERY_FROM_ENABLED_JOBS_MATCHING_FILTERS
    )
    Page<DistributionJobEntity> findAndSortEnabledJobsMatchingFilters(
        @Param("blackDuckConfigId") Long blackDuckConfigId,
        @Param("frequencies") Collection<String> frequencies,
        @Param("notificationTypeSet") Set<String> notificationTypeSet,
        @Param("projectNames") Set<String> projectNames,
        @Param("policyNames") Set<String> policyNames,
        @Param("vulnerabilitySeverities") Set<String> vulnerabilitySeverities,
        Pageable pageable
    );

}
