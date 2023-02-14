package com.synopsys.integration.alert.database.job.execution;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobCompletionRepository extends JpaRepository<JobCompletionStatusEntity, UUID> {
    @Query("SELECT entity FROM JobCompletionStatusEntity entity "
        + "LEFT JOIN DistributionJobEntity jobConfiguration ON entity.jobConfigId = jobConfiguration.jobId "
        + "WHERE jobConfiguration.name LIKE %:searchTerm% "
        + "OR LOWER(entity.latestStatus) LIKE %:searchTerm% "
        + "OR COALESCE(to_char(entity.lastRun, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%")
    Page<JobCompletionStatusEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
