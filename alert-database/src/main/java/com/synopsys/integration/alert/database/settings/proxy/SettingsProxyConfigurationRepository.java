/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.settings.proxy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsProxyConfigurationRepository extends JpaRepository<SettingsProxyConfigurationEntity, UUID> {
    Optional<SettingsProxyConfigurationEntity> findByName(String name);
    
    void deleteByName(String name);
}
