/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.distribution;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.job.DistributionJobEntity;

@Component
public interface DistributionRepository extends JpaRepository<DistributionJobEntity, UUID> {

    @Query(value = "SELECT new com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity(job.jobId, job.enabled, job.name, job.channelDescriptorName, job.distributionFrequency, audit.timeLastSent, audit.status) "
                       + " FROM DistributionJobEntity job"
                       + " LEFT JOIN AuditEntryEntity audit ON job.jobId = audit.commonConfigId"
    )
    DistributionWithAuditEntity getDistributionWithAuditInfo();
    
}
