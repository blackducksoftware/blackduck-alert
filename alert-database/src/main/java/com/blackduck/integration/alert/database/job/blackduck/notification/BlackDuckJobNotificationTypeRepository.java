/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.blackduck.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlackDuckJobNotificationTypeRepository extends JpaRepository<BlackDuckJobNotificationTypeEntity, BlackDuckJobNotificationTypePK> {
    @Query("DELETE FROM BlackDuckJobNotificationTypeEntity entity"
               + " WHERE entity.jobId = :jobId"
    )
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void bulkDeleteAllByJobId(@Param("jobId") UUID jobId);

    List<BlackDuckJobNotificationTypeEntity> findByJobId(UUID jobId);

}
