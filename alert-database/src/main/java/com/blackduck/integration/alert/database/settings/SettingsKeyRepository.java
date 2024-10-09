package com.blackduck.integration.alert.database.settings;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsKeyRepository extends JpaRepository<SettingsKeyEntity, Long> {

    Optional<SettingsKeyEntity> findByKey(String key);
}
