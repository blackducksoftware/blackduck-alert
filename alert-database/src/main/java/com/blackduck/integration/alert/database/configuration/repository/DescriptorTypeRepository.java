/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.configuration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackduck.integration.alert.database.configuration.DescriptorTypeEntity;

public interface DescriptorTypeRepository extends JpaRepository<DescriptorTypeEntity, Long> {
    Optional<DescriptorTypeEntity> findFirstByType(String descriptorType);
}
