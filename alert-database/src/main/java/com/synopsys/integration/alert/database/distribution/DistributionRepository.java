/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.distribution;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.synopsys.integration.alert.database.job.DistributionJobEntity;

public interface DistributionRepository extends JpaRepository<DistributionJobEntity, UUID> {

    @Query("SELECT new com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, audit.timeLastSent, audit.status) "
               + " FROM DistributionJobEntity job"
               + " LEFT JOIN AuditEntryEntity audit ON job.jobId = audit.commonConfigId"
               + " WHERE audit.timeLastSent IN (SELECT MAX(auditNested.timeLastSent) FROM AuditEntryEntity auditNested WHERE auditNested.commonConfigId = job.id GROUP BY auditNested.commonConfigId)"
    )
    Page<DistributionWithAuditEntity> getDistributionWithAuditInfo(Pageable pageable);

    /*
    select job.job_id, job.name, max(audit.time_last_sent)
from alert.distribution_jobs job
left outer join alert.audit_entries audit on audit.common_config_id = job.job_id
group by job.job_id
order by job.name limit 10;
     */
    //    @Query(value =
    //               "SELECT NEW com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, MAX(audit.timeLastSent), audit.status)"
    //                   + " FROM DistributionJobEntity job"
    //                   + " LEFT OUTER JOIN AuditEntryEntity audit ON audit.commonConfigId = job.jobId"
    //                   + " GROUP BY job.jobId, audit.status"
    //    )
    @Query("SELECT NEW com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, audit.timeLastSent, audit.status)"
               + " FROM DistributionJobEntity job"
               + " LEFT OUTER JOIN AuditEntryEntity audit ON audit.commonConfigId = job.jobId"
    )
    Page<DistributionWithAuditEntity> getDistributionWithAuditInfoGavin(Pageable pageable);

    // UUID jobId, String name, Boolean enabled, String distributionFrequency, String processingType, String channelDescriptorName, OffsetDateTime createdAt, OffsetDateTime lastUpdated
    @Query(
        //               "SELECT NEW com.synopsys.integration.alert.database.job.DistributionJobEntity(job.jobId, job.name, job.enabled, job.distributionFrequency, job.processingType, job.channelDescriptorName, job.createdAt, job.lastUpdated)"
        "SELECT job FROM DistributionJobEntity job"
    )
    List<DistributionJobEntity> getJobEntities();
}
