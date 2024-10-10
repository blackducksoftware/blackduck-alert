/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.database.configuration.DescriptorFieldRelation;
import com.blackduck.integration.alert.database.configuration.key.DescriptorFieldRelationPK;

@Component
public interface DescriptorFieldRepository extends JpaRepository<DescriptorFieldRelation, DescriptorFieldRelationPK> {
    List<DescriptorFieldRelation> findByDescriptorId(Long descriptorId);

    List<DescriptorFieldRelation> findByFieldId(Long fieldId);
}
