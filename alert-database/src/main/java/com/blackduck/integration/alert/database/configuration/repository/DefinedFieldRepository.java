/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.configuration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.database.configuration.DefinedFieldEntity;

@Component
public interface DefinedFieldRepository extends JpaRepository<DefinedFieldEntity, Long> {
    Optional<DefinedFieldEntity> findFirstByKey(String fieldKey);

    @Query(value = "SELECT entity FROM DefinedFieldEntity entity "
                       + "INNER JOIN entity.descriptorFieldRelations relation ON entity.id = relation.fieldId "
                       + "INNER JOIN entity.fieldContextRelations fieldContextRelation ON entity.id = fieldContextRelation.fieldId "
                       + "WHERE relation.descriptorId = ?1 AND fieldContextRelation.contextId = ?2")
    List<DefinedFieldEntity> findByDescriptorIdAndContext(Long descriptorId, Long contextId);
}
