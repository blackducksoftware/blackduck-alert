package com.blackduck.integration.alert.channel.email.database.configuration.properties;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailConfigurationPropertiesRepository extends JpaRepository<EmailConfigurationsPropertyEntity, EmailConfigurationPropertyPK> {

    @Query(
        "DELETE FROM EmailConfigurationsPropertyEntity entity"
            + " WHERE entity.configurationId = :configurationId"
    )
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void bulkDeleteByConfigurationId(@Param("configurationId") UUID configurationId);
}
