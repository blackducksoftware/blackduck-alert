/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.provider.task;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTaskPropertiesRepository extends JpaRepository<ProviderTaskPropertiesEntity, Long> {
    Optional<ProviderTaskPropertiesEntity> findByTaskNameAndPropertyName(String taskName, String propertyName);

}
