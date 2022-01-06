/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.task;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTaskPropertiesRepository extends JpaRepository<ProviderTaskPropertiesEntity, Long> {
    Optional<ProviderTaskPropertiesEntity> findByTaskNameAndPropertyName(String taskName, String propertyName);

}
