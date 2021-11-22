/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.blackduck.policy;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BlackDuckJobPolicyFilterRepository extends JpaRepository<BlackDuckJobPolicyFilterEntity, BlackDuckJobPolicyFilterPK> {
    @Query("DELETE FROM BlackDuckJobPolicyFilterEntity entity"
               + " WHERE entity.jobId = :jobId"
    )
    @Modifying
    void bulkDeleteAllByJobId(UUID jobId);

    List<BlackDuckJobPolicyFilterEntity> findByJobId(UUID jobId);

}
