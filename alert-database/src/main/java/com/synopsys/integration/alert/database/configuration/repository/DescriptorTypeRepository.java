/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synopsys.integration.alert.database.configuration.DescriptorTypeEntity;

public interface DescriptorTypeRepository extends JpaRepository<DescriptorTypeEntity, Long> {
    Optional<DescriptorTypeEntity> findFirstByType(String descriptorType);
}
