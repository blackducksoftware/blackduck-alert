package com.synopsys.integration.alert.channel.github.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GitHubConfigurationRepository extends JpaRepository<GitHubConfigurationEntity, UUID>  {
    Optional<GitHubConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByConfigurationId(UUID uuid);

    @Query("SELECT githubEntity FROM GitHubConfigurationEntity githubEntity"
        + " WHERE githubEntity.name LIKE %:searchTerm%"
        + "   OR COALESCE(to_char(githubEntity.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%"
        + "   OR (githubEntity.lastUpdated != NULL AND COALESCE(to_char(githubEntity.lastUpdated, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%)"
    )
    Page<GitHubConfigurationEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
