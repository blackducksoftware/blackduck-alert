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

import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.database.configuration.RegisteredDescriptorEntity;

@Component
public interface RegisteredDescriptorRepository extends JpaRepository<RegisteredDescriptorEntity, Long> {
    Optional<RegisteredDescriptorEntity> findFirstByName(String descriptorName);

    List<RegisteredDescriptorEntity> findByTypeId(Long descriptorTypeId);

    @Query(value = "SELECT entity FROM RegisteredDescriptorEntity entity "
                       + "INNER JOIN entity.descriptorConfigEntities descriptorConfigEntity ON entity.id = descriptorConfigEntity.descriptorId "
                       + "INNER JOIN descriptorConfigEntity.fieldValueEntities fieldValueEntity ON descriptorConfigEntity.id = fieldValueEntity.configId "
                       + "INNER JOIN fieldValueEntity.definedFieldEntity definedFieldEntity ON definedFieldEntity.id = fieldValueEntity.fieldId "
                       + "WHERE entity.typeId = ?1 AND definedFieldEntity.key = '" + ChannelDescriptor.KEY_FREQUENCY + "' AND fieldValueEntity.value = ?2")
    List<RegisteredDescriptorEntity> findByTypeIdAndFrequency(Long descriptorTypeId, String frequency);

}
