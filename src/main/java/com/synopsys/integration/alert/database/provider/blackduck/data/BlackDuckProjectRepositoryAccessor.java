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
package com.synopsys.integration.alert.database.provider.blackduck.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Component
@Transactional
public class BlackDuckProjectRepositoryAccessor extends RepositoryAccessor {
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
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final BlackDuckProjectEntity blackDuckProjectEntity = (BlackDuckProjectEntity) entity;
        return blackDuckProjectRepository.save(blackDuckProjectEntity);
    }

    public List<BlackDuckProjectEntity> deleteAndSaveAll(final Iterable<BlackDuckProjectEntity> blackDuckProjectEntities) {
        blackDuckProjectRepository.deleteAllInBatch();
        return blackDuckProjectRepository.saveAll(blackDuckProjectEntities);
    }
}
