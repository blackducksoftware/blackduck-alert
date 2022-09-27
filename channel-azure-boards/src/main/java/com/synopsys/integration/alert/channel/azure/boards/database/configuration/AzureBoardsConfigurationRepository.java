package com.synopsys.integration.alert.channel.azure.boards.database.configuration;

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
        + " WHERE azureEntity.name LIKE %:searchTerm%"
        + "   OR azureEntity.url LIKE %:searchTerm%"
        + "   OR COALESCE(to_char(azureEntity.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%"
        + "   OR (azureEntity.lastUpdated != NULL AND COALESCE(to_char(azureEntity.lastUpdated, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm%)"
    )
    Page<AzureBoardsConfigurationEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
