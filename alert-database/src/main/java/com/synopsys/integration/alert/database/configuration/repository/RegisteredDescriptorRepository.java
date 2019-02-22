/**
 * alert-database
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.configuration.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;

@Component
public interface RegisteredDescriptorRepository extends JpaRepository<RegisteredDescriptorEntity, Long> {
    Optional<RegisteredDescriptorEntity> findFirstByName(final String descriptorName);

    List<RegisteredDescriptorEntity> findByTypeId(Long descriptorTypeId);

    @Query(value = "SELECT entity FROM RegisteredDescriptorEntity entity "
                       + "INNER JOIN entity.descriptorConfigEntities config ON entity.id = config.descriptorId "
                       + "INNER JOIN config.configGroupEntity configGroup ON config.id = configGroup.configId "
                       + "WHERE configGroup.jobId = ?1")
    List<RegisteredDescriptorEntity> findByJobId(final UUID jobId);

    @Query(value = "SELECT entity FROM RegisteredDescriptorEntity entity "
                       + "INNER JOIN entity.descriptorConfigEntities descriptorConfigEntity ON entity.id = descriptorConfigEntity.descriptorId "
                       + "INNER JOIN descriptorConfigEntity.fieldValueEntities fieldValueEntity ON descriptorConfigEntity.id = fieldValueEntity.configId "
                       + "INNER JOIN fieldValueEntity.definedFieldEntity definedFieldEntity ON definedFieldEntity.id = fieldValueEntity.fieldId "
                       + "WHERE entity.typeId = ?1 AND definedFieldEntity.key = '" + ChannelDistributionUIConfig.KEY_FREQUENCY + "' AND fieldValueEntity.value = ?2")
    List<RegisteredDescriptorEntity> findByTypeIdAndFrequency(Long descriptorTypeId, String frequency);
}
