/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.settings;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsKeyRepository extends JpaRepository<SettingsKeyEntity, Long> {

    Optional<SettingsKeyEntity> findByKey(String key);
}
