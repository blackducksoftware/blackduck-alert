/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.jpa.TypedParameterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vladmihalcea.hibernate.type.array.StringArrayType;

@Component
public class DistributionJobNotificationFilterRepository {
    private static final String POST_SELECT_QUERY_SECTION =
        "FROM alert.distribution_jobs job_entity"
            + "    LEFT OUTER JOIN alert.blackduck_job_details bd_details ON job_entity.job_id = bd_details.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_notification_types notif_type ON job_entity.job_id = notif_type.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_policy_filters policy_filter ON job_entity.job_id = policy_filter.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_vulnerability_severity_filters vuln_filter ON job_entity.job_id = vuln_filter.job_id"
            + "    LEFT OUTER JOIN alert.blackduck_job_projects projects ON job_entity.job_id = projects.job_id"
            + "    WHERE job_entity.enabled = true"
            + "    AND bd_details.global_config_id = :blackDuckConfigId"
            + "    AND notif_type.notification_type IN (:notificationTypes)"
            + "    AND job_entity.distribution_frequency IN (:frequencies)"
            + "    AND ("
            + "        (bd_details.filter_by_project = false AND coalesce(:projectNames, NULL) IS NULL)"
            + "        OR ("
            + "            coalesce(:projectNames, NULL) IS NOT NULL"
            + "            AND ("
            + "                projects.project_name IN (:projectNames)"
            + "                OR ("
            + "                    bd_details.project_name_pattern IS NOT NULL AND EXISTS ("
            + "                        SELECT 1 FROM unnest(:projectNamesArray) "
            + "                        WHERE unnest ~ bd_details.project_name_pattern"
            + "                    )"
            + "                )"
            + "            )"
            + "        )"
            + "    ) AND ("
            + "        ("
            + "            coalesce(:vulnerabilitySeverities, NULL) IS NULL"
            + "            OR vuln_filter.severity_name IS NULL"
            + "            OR vuln_filter.severity_name IN (:vulnerabilitySeverities)"
            + "        ) OR ("
            + "            coalesce(:policyNames, NULL) IS NULL"
            + "            OR policy_filter.policy_name IS NULL"
            + "            OR policy_filter.policy_name IN (:policyNames)"
            + "        )"
            + "    )"
            + "    GROUP BY job_entity.job_id"
            + "    ORDER BY job_entity.created_at ASC";
    private static final String FULL_QUERY =
        "SELECT job_entity.job_id, job_entity.name, job_entity.enabled, job_entity.distribution_frequency, job_entity.processing_type, job_entity.channel_descriptor_name, job_entity.created_at, job_entity.last_updated "
            + POST_SELECT_QUERY_SECTION;
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM (SELECT job_entity.job_id " + POST_SELECT_QUERY_SECTION + ") distinct_job";

    private final EntityManager entityManager;

    @Autowired
    public DistributionJobNotificationFilterRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<DistributionJobEntity> findAndSortEnabledJobsMatchingFilters(
        Long blackDuckConfigId,
        Collection<String> frequencies,
        Set<String> notificationTypes,
        Set<String> projectNames,
        Set<String> policyNames,
        Set<String> vulnerabilitySeverities,
        Pageable pageable
    ) {
        Query fullQuery = entityManager.createNativeQuery(FULL_QUERY, DistributionJobEntity.class);
        bindParameters(fullQuery, blackDuckConfigId, frequencies, notificationTypes, projectNames, policyNames, vulnerabilitySeverities);

        // Paging
        fullQuery.setMaxResults(pageable.getPageSize());
        fullQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        List<DistributionJobEntity> fullQueryResults = fullQuery.getResultList();

        // Count query
        Query countQuery = entityManager.createNativeQuery(COUNT_QUERY);
        bindParameters(countQuery, blackDuckConfigId, frequencies, notificationTypes, projectNames, policyNames, vulnerabilitySeverities);
        BigInteger totalResults = (BigInteger) countQuery.getSingleResult();

        return new PageImpl<>(fullQueryResults, pageable, totalResults.intValue());
    }

    private void bindParameters(
        Query query,
        Long blackDuckConfigId,
        Collection<String> frequencies,
        Set<String> notificationTypes,
        Set<String> projectNames,
        Set<String> policyNames,
        Set<String> vulnerabilitySeverities
    ) {
        query.setParameter("blackDuckConfigId", blackDuckConfigId);
        query.setParameter("frequencies", frequencies);
        query.setParameter("notificationTypes", notificationTypes);
        query.setParameter("projectNames", projectNames);

        String[] projectNamesArray;
        if (projectNames.isEmpty()) {
            projectNamesArray = new String[0];
        } else {
            projectNamesArray = new String[projectNames.size()];
            projectNames.toArray(projectNamesArray);
        }
        query.setParameter("projectNamesArray", new TypedParameterValue(StringArrayType.INSTANCE, projectNamesArray));
        query.setParameter("policyNames", policyNames);
        query.setParameter("vulnerabilitySeverities", vulnerabilitySeverities);
    }

}
