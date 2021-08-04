/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.slack;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

public interface SlackJobDetailsRepository extends JpaRepository<SlackJobDetailsEntity, UUID> {

    @Query(
        "SELECT new SlackJobDetailsModel("
            + "details.job_id, jobs.name, details.webhook, details.channel_name, details.channel_username)"
            + " FROM slack_job_details details"
            + " INNER JOIN distribution_jobs jobs ON jobs.job_id = details.job_id"
            + " WHERE details.job_id = ?1"
    )
    Optional<SlackJobDetailsModel> findJobDetailsWithNameById(UUID jobId);
}
