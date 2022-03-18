/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JiraServerConfigurationRepository extends JpaRepository<JiraServerConfigurationEntity, UUID> {
    Optional<JiraServerConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByConfigurationId(UUID uuid);

    @Query("SELECT jiraEntity FROM JiraServerConfigurationEntity jiraEntity"
        + " WHERE jiraEntity.name LIKE %:searchTerm%"
        + "   OR jiraEntity.url LIKE %:searchTerm%"
        + "   OR COALESCE(to_char(jiraEntity.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%"
        + "   OR (jiraEntity.lastUpdated != NULL AND COALESCE(to_char(jiraEntity.lastUpdated, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%)"
    )
    Page<JiraServerConfigurationEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
