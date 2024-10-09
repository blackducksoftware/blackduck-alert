package com.blackduck.integration.alert.database.settings.proxy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsProxyConfigurationRepository extends JpaRepository<SettingsProxyConfigurationEntity, UUID> {
    Optional<SettingsProxyConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
