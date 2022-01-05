/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.configuration.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.configuration.key.DescriptorFieldRelationPK;

@Component
public interface DescriptorFieldRepository extends JpaRepository<DescriptorFieldRelation, DescriptorFieldRelationPK> {
    List<DescriptorFieldRelation> findByDescriptorId(Long descriptorId);

    List<DescriptorFieldRelation> findByFieldId(Long fieldId);
}
