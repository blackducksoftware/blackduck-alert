/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobToNotificationRelationRepository extends JpaRepository<JobToNotificationRelation, JobToNotificationRelationPK> {

    Page<JobToNotificationRelation> findAllByCorrelationIdAndJobId(UUID correlationId, UUID jobId, Pageable pageable);

    void deleteAllByCorrelationIdAndJobId(UUID correlationId, UUID jobId);
}
