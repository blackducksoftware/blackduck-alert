/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.database.configuration.FieldValueEntity;

@Component
public interface FieldValueRepository extends JpaRepository<FieldValueEntity, Long> {
    List<FieldValueEntity> findByConfigId(Long configId);

    List<FieldValueEntity> findAllByFieldIdAndValue(Long fieldId, String value);

}
