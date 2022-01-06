/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;

@Component
public interface DescriptorConfigRepository extends JpaRepository<DescriptorConfigEntity, Long> {
    List<DescriptorConfigEntity> findByDescriptorId(Long descriptorId);

    List<DescriptorConfigEntity> findByDescriptorIdAndContextId(Long descriptorId, Long contextId);

    Page<DescriptorConfigEntity> findByDescriptorIdAndContextId(Long descriptorId, Long contextId, Pageable pageable);

    @Query("SELECT config FROM DescriptorConfigEntity config"
               + " LEFT JOIN config.registeredDescriptorEntity descriptor"
               + " WHERE descriptor.name = :descriptorName"
    )
    List<DescriptorConfigEntity> findByDescriptorName(@Param("descriptorName") String descriptorName);

    @Query("SELECT config FROM DescriptorConfigEntity config"
               + " LEFT JOIN config.registeredDescriptorEntity descriptor"
               + " LEFT JOIN DescriptorTypeEntity type ON type.id = descriptor.typeId"
               + " WHERE type.type = :descriptorType"
    )
    List<DescriptorConfigEntity> findByDescriptorType(@Param("descriptorType") String descriptorType);

}
