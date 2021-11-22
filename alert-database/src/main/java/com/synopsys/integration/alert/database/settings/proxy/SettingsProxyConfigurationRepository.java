package com.synopsys.integration.alert.database.settings.proxy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsProxyConfigurationRepository extends JpaRepository<SettingsProxyConfigurationEntity, UUID> {
    
}
