/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.distribution;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.synopsys.integration.alert.database.job.DistributionJobEntity;

public interface DistributionRepository extends JpaRepository<DistributionJobEntity, UUID> {

    @Query(
        "SELECT NEW com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, MAX(audit.timeLastSent) AS lastSent, audit.status)"
            + " FROM DistributionJobEntity job"
            + " LEFT OUTER JOIN com.synopsys.integration.alert.database.audit.AuditEntryEntity audit ON audit.commonConfigId = job.jobId"
            + " WHERE job.channelDescriptorName IN (:channelDescriptorNames)"
            + " GROUP BY job.jobId, audit.status, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency"
    )
    Page<DistributionWithAuditEntity> getDistributionWithAuditInfo(Pageable pageable, @Param("channelDescriptorNames") Collection<String> channelDescriptorNames);

    @Query(
        "SELECT NEW com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, MAX(audit.timeLastSent) AS lastSent, audit.status)"
            + " FROM DistributionJobEntity job"
            + " LEFT OUTER JOIN com.synopsys.integration.alert.database.audit.AuditEntryEntity audit ON audit.commonConfigId = job.jobId"
            + " WHERE job.channelDescriptorName IN (:channelDescriptorNames) AND job.name LIKE %:searchTerm%"
            + " GROUP BY job.jobId, audit.status, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency"
    )
    Page<DistributionWithAuditEntity> getDistributionWithAuditInfoWithSearch(Pageable pageable, @Param("channelDescriptorNames") Collection<String> channelDescriptorNames, @Param("searchTerm") String searchTerm);

}
