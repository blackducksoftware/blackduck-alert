/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.blackduck.projects;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackDuckJobProjectRepository extends JpaRepository<BlackDuckJobProjectEntity, BlackDuckJobProjectPK> {
    void deleteAllByJobId(UUID jobId);

    List<BlackDuckJobProjectEntity> findByJobId(UUID jobId);

}
