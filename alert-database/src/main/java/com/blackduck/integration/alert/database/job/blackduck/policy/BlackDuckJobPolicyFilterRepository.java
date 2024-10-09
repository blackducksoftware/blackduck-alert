package com.blackduck.integration.alert.database.job.blackduck.policy;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlackDuckJobPolicyFilterRepository extends JpaRepository<BlackDuckJobPolicyFilterEntity, BlackDuckJobPolicyFilterPK> {
    @Query("DELETE FROM BlackDuckJobPolicyFilterEntity entity"
               + " WHERE entity.jobId = :jobId"
    )
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void bulkDeleteAllByJobId(@Param("jobId") UUID jobId);

    List<BlackDuckJobPolicyFilterEntity> findByJobId(UUID jobId);

}
