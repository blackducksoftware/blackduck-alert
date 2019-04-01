/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.database.provider.blackduck.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.RepositoryAccessor;

@Component
@Transactional
public class BlackDuckProjectRepositoryAccessor extends RepositoryAccessor<BlackDuckProjectEntity> {
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private final BlackDuckProjectRepository blackDuckProjectRepository;

    @Autowired
    public BlackDuckProjectRepositoryAccessor(final BlackDuckProjectRepository blackDuckProjectRepository) {
        super(blackDuckProjectRepository);
        this.blackDuckProjectRepository = blackDuckProjectRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public BlackDuckProjectEntity findByName(final String name) {
        return blackDuckProjectRepository.findByName(name);
    }

    @Override
    public BlackDuckProjectEntity saveEntity(final BlackDuckProjectEntity blackDuckProjectEntity) {
        final BlackDuckProjectEntity trimmedBlackDuckProjectEntity = trimDescription(blackDuckProjectEntity);
        return super.saveEntity(trimmedBlackDuckProjectEntity);
    }

    public List<BlackDuckProjectEntity> deleteAndSaveAll(final Iterable<BlackDuckProjectEntity> blackDuckProjectEntities) {
        blackDuckProjectRepository.deleteAllInBatch();
        return saveAll(blackDuckProjectEntities);
    }

    public List<BlackDuckProjectEntity> saveAll(final Iterable<BlackDuckProjectEntity> blackDuckProjectEntities) {
        final List<BlackDuckProjectEntity> trimmedEntities = StreamSupport.stream(blackDuckProjectEntities.spliterator(), false)
                                                                 .map(this::trimDescription)
                                                                 .collect(Collectors.toList());
        return blackDuckProjectRepository.saveAll(trimmedEntities);
    }

    private BlackDuckProjectEntity trimDescription(final BlackDuckProjectEntity blackDuckProjectEntity) {
        final String trimmedDescription = StringUtils.abbreviate(blackDuckProjectEntity.getDescription(), MAX_DESCRIPTION_LENGTH);
        return new BlackDuckProjectEntity(blackDuckProjectEntity.getName(), trimmedDescription, blackDuckProjectEntity.getHref(), blackDuckProjectEntity.getProjectOwnerEmail());
    }

}
