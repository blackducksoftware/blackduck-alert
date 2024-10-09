package com.blackduck.integration.alert.database.job;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobToNotificationRelationRepository extends JpaRepository<JobToNotificationRelation, JobToNotificationRelationPK> {

    Page<JobToNotificationRelation> findAllByCorrelationIdAndJobId(UUID correlationId, UUID jobId, Pageable pageable);

    void deleteAllByCorrelationIdAndJobId(UUID correlationId, UUID jobId);

    @Query(
        "SELECT DISTINCT entity.jobId FROM JobToNotificationRelation entity"
            + " WHERE entity.correlationId = ?1"
    )
    Set<UUID> findDistinctJobIdByCorrelationId(UUID correlationId);

    int countAllByCorrelationId(UUID correlationId);

    int countAllByCorrelationIdAndJobId(UUID correlationId, UUID jobId);
}
