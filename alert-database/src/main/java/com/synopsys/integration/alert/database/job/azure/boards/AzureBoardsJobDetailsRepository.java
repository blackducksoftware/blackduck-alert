/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.azure.boards;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

public interface AzureBoardsJobDetailsRepository extends JpaRepository<AzureBoardsJobDetailsEntity, UUID> {

    @Query(
        "SELECT new AzureBoardsJobDetailsModel("
            + "details.job_id, jobs.name, details.add_comments, details.project_name_or_id, details.work_item_type, details.work_item_completed_state, work_item_reopen_state)"
            + " FROM azure_boards_job_details details"
            + " INNER JOIN distribution_jobs jobs ON jobs.job_id = details.job_id"
            + " WHERE details.job_id = ?1"
    )
    Optional<AzureBoardsJobDetailsModel> findJobDetailsWithNameById(UUID jobId);
}
