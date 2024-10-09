package com.blackduck.integration.alert.channel.jira.server.database.configuration;

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

    @Query(value = "SELECT * FROM alert.configuration_jira_server AS jira_server_config"
        + " WHERE jira_server_config.name ILIKE %:searchTerm%"
        + "   OR jira_server_config.url ILIKE %:searchTerm%"
        + "   OR (SELECT name FROM alert.jira_server_authorization_method WHERE jira_server_config.authorization_method = alert.jira_server_authorization_method.id) ILIKE %:searchTerm%"
        + "   OR COALESCE(to_char(jira_server_config.created_at, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%"
        + "   OR (jira_server_config.last_updated IS NOT NULL AND COALESCE(to_char(jira_server_config.last_updated, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%)",
        nativeQuery = true
    )
    Page<JiraServerConfigurationEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
