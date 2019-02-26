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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;

@Component
@Transactional
public class ProviderProjectRepositoryAccessor extends RepositoryAccessor<ProviderProjectEntity> {
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private final ProviderProjectRepository providerProjectRepository;

    @Autowired
    public ProviderProjectRepositoryAccessor(final ProviderProjectRepository providerProjectRepository) {
        super(providerProjectRepository);
        this.providerProjectRepository = providerProjectRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public ProviderProjectEntity findByName(final String name) {
        return providerProjectRepository.findByName(name);
    }

    // TODO test
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderProjectEntity> findByProviderName(final String providerName) {
        return providerProjectRepository.findByProvider(providerName);
    }

    @Override
    public ProviderProjectEntity saveEntity(final ProviderProjectEntity providerProjectEntity) {
        final String trimmedDescription = StringUtils.abbreviate(providerProjectEntity.getDescription(), MAX_DESCRIPTION_LENGTH);
        final ProviderProjectEntity trimmedBlackDuckProjectEntity = new ProviderProjectEntity(
            providerProjectEntity.getName(), trimmedDescription, providerProjectEntity.getHref(), providerProjectEntity.getProjectOwnerEmail(), providerProjectEntity.getProvider());
        return super.saveEntity(trimmedBlackDuckProjectEntity);
    }

    public List<ProviderProjectEntity> deleteAndSaveAll(final Iterable<ProviderProjectEntity> blackDuckProjectEntities) {
        providerProjectRepository.deleteAllInBatch();
        return providerProjectRepository.saveAll(blackDuckProjectEntities);
    }

}
