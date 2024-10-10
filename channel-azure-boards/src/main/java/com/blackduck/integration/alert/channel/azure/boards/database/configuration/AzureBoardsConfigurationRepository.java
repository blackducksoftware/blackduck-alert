/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AzureBoardsConfigurationRepository extends JpaRepository<AzureBoardsConfigurationEntity, UUID> {
    Optional<AzureBoardsConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByConfigurationId(UUID uuid);

    @Query("SELECT azureEntity FROM AzureBoardsConfigurationEntity azureEntity"
        + " WHERE azureEntity.name ILIKE %:searchTerm%"
        + "   OR azureEntity.organizationName ILIKE %:searchTerm%"
        + "   OR COALESCE(to_char(azureEntity.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%"
        + "   OR (azureEntity.lastUpdated IS NOT NULL AND COALESCE(to_char(azureEntity.lastUpdated, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%)"
    )
    Page<AzureBoardsConfigurationEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
