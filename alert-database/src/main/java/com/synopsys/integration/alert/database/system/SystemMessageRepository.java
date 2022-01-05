/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.system;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SystemMessageRepository extends JpaRepository<SystemMessageEntity, Long> {
    @Query("SELECT message FROM SystemMessageEntity message WHERE message.created >= ?1 AND message.created < ?2 ORDER BY message.created asc")
    List<SystemMessageEntity> findByCreatedBetween(OffsetDateTime start, OffsetDateTime end);

    SystemMessageEntity findTopByOrderByCreatedAsc();

    List<SystemMessageEntity> findByType(String type);

    @Query("DELETE FROM SystemMessageEntity message"
               + " WHERE message.created < :date"
    )
    @Modifying
    int bulkDeleteCreatedBefore(@Param("date") OffsetDateTime date);

}
