/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.distribution;

import java.sql.Timestamp;
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
        value =
            "SELECT CAST(job.job_id as varchar) AS id, job.enabled, job.name, job.channel_descriptor_name, job.distribution_frequency, executionStatus.last_run AS time_last_sent, executionStatus.latest_status AS status"
                + " FROM alert.distribution_jobs AS job"
                + " LEFT JOIN alert.job_execution_status AS executionStatus ON executionStatus.job_config_id = job.job_id"
                + " WHERE job.channel_descriptor_name IN (:channelDescriptorNames)",
        nativeQuery = true
    )
    Page<DistributionDBResponse> getDistributionWithAuditInfo(Pageable pageable, @Param("channelDescriptorNames") Collection<String> channelDescriptorNames);

    @Query(
        value =
            "SELECT CAST(job.job_id as varchar) AS id, job.enabled, job.name, job.channel_descriptor_name, job.distribution_frequency, executionStatus.last_run AS time_last_sent, executionStatus.latest_status AS status"
                + " FROM alert.distribution_jobs AS job"
                + " LEFT JOIN alert.job_execution_status AS executionStatus ON executionStatus.job_config_id = job.job_id"
                + " WHERE job.channel_descriptor_name IN (:channelDescriptorNames) AND job.name LIKE %:searchTerm%",
        nativeQuery = true
    )
    Page<DistributionDBResponse> getDistributionWithAuditInfoWithSearch(
        Pageable pageable,
        @Param("channelDescriptorNames") Collection<String> channelDescriptorNames,
        @Param("searchTerm") String searchTerm
    );
    
    interface DistributionDBResponse {
        String getId();

        Boolean getEnabled();

        String getName();

        String getChannel_Descriptor_Name();

        String getDistribution_Frequency();

        Timestamp getTime_Last_Sent();

        String getStatus();

    }
}
