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
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;

@Component
public class ProviderUserProjectRelationRepositoryAccessor {
    private final ProviderUserProjectRelationRepository providerUserProjectRelationRepository;

    @Autowired
    public ProviderUserProjectRelationRepositoryAccessor(final ProviderUserProjectRelationRepository providerUserProjectRelationRepository) {
        this.providerUserProjectRelationRepository = providerUserProjectRelationRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderUserProjectRelation> readEntities() {
        return providerUserProjectRelationRepository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderUserProjectRelation> findByProviderProjectId(final Long providerProjectId) {
        return providerUserProjectRelationRepository.findByProviderProjectId(providerProjectId);
    }

    @Transactional
    public List<ProviderUserProjectRelation> deleteAndSaveAll(final Set<ProviderUserProjectRelation> userProjectRelations) {
        providerUserProjectRelationRepository.deleteAllInBatch();
        return providerUserProjectRelationRepository.saveAll(userProjectRelations);
    }
}
