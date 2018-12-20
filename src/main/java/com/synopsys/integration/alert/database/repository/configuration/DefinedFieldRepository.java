/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.database.repository.configuration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;

@Component
public interface DefinedFieldRepository extends JpaRepository<DefinedFieldEntity, Long> {
    Optional<DefinedFieldEntity> findFirstByKey(final String fieldKey);

    @Query(value = "SELECT entity FROM DefinedFieldEntity entity INNER JOIN entity.descriptorFieldRelations relation ON entity.id = relation.fieldId "
                       + "INNER JOIN entity.fieldContextRelations fieldContextRelation ON entity.id = fieldContextRelation.fieldId "
                       + "WHERE relation.descriptorId = ?1 AND fieldContextRelation.contextId = ?2")
    List<DefinedFieldEntity> findByDescriptorIdAndContext(Long descriptorId, Long contextId);
}
