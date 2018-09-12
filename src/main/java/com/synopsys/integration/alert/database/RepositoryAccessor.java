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
package com.synopsys.integration.alert.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Transactional
public abstract class RepositoryAccessor {
    private final JpaRepository<? extends DatabaseEntity, Long> repository;

    public RepositoryAccessor(final JpaRepository<? extends DatabaseEntity, Long> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<? extends DatabaseEntity> readEntities() {
        return repository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return repository.findById(id);
    }

    public void deleteEntity(final long id) {
        repository.deleteById(id);
    }

    public abstract DatabaseEntity saveEntity(final DatabaseEntity entity);
}
