/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;

@Component
public interface DescriptorConfigRepository extends JpaRepository<DescriptorConfigEntity, Long> {
    List<DescriptorConfigEntity> findByDescriptorId(Long descriptorId);

    List<DescriptorConfigEntity> findByContextId(Long contextId);

    List<DescriptorConfigEntity> findByDescriptorIdAndContextId(Long descriptorId, Long contextId);
}
