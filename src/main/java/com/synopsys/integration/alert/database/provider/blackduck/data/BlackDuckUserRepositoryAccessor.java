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

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Component
public class BlackDuckUserRepositoryAccessor extends RepositoryAccessor {
    private final BlackDuckUserRepository blackDuckUserRepository;

    @Autowired
    public BlackDuckUserRepositoryAccessor(final BlackDuckUserRepository blackDuckUserRepository) {
        super(blackDuckUserRepository);
        this.blackDuckUserRepository = blackDuckUserRepository;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final BlackDuckUserEntity blackDuckUserEntity = (BlackDuckUserEntity) entity;
        return blackDuckUserRepository.save(blackDuckUserEntity);
    }

    public List<BlackDuckUserEntity> deleteAndSaveAll(final Iterable<BlackDuckUserEntity> userEntitiesToDelete, final Iterable<BlackDuckUserEntity> userEntitiesToAdd) {
        blackDuckUserRepository.deleteAll(userEntitiesToDelete);
        return blackDuckUserRepository.saveAll(userEntitiesToAdd);
    }
}
