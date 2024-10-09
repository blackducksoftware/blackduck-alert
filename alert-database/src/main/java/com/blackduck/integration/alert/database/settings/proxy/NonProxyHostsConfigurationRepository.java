package com.blackduck.integration.alert.database.settings.proxy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NonProxyHostsConfigurationRepository extends JpaRepository<NonProxyHostConfigurationEntity, NonProxyHostConfigurationPK> {

    @Query(
        "DELETE FROM NonProxyHostConfigurationEntity entity"
            + " WHERE entity.configurationId = :configurationId"
    )
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void bulkDeleteByConfigurationId(@Param("configurationId") UUID configurationId);
}
