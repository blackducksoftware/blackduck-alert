/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.jira.cloud.custom_field;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraCloudJobCustomFieldRepository extends JpaRepository<JiraCloudJobCustomFieldEntity, JiraCloudJobCustomFieldPK> {
    List<JiraCloudJobCustomFieldEntity> findByJobId(UUID jobId);

    void deleteByJobId(UUID jobId);

}
