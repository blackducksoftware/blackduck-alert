/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.distribution;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.synopsys.integration.alert.database.job.DistributionJobEntity;

public interface DistributionRepository extends JpaRepository<DistributionJobEntity, UUID> {

    @Query("SELECT NEW com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, MAX(audit.timeLastSent), audit.status)"
               + " FROM DistributionJobEntity job"
               + " LEFT OUTER JOIN com.synopsys.integration.alert.database.audit.AuditEntryEntity audit ON audit.commonConfigId = job.jobId"
               + " GROUP BY job.jobId, audit.status"
               + " ORDER BY job.name"
    )
    Page<DistributionWithAuditEntity> getDistributionWithAuditInfo(Pageable pageable);
}
