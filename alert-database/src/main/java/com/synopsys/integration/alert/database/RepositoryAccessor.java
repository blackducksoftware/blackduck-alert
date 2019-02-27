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
package com.synopsys.integration.alert.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
// TODO we should strongly consider only allowing reads from a repository accessor; deletes should not necessarily be atomic transactions
public abstract class RepositoryAccessor<D extends DatabaseEntity> {
    private final JpaRepository<D, Long> repository;

    public RepositoryAccessor(final JpaRepository<D, Long> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<D> readEntities() {
        return repository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<D> readEntity(final Long id) {
        return repository.findById(id);
    }

    public void deleteEntity(final Long id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAllInBatch();
    }

    public D saveEntity(final D entity) {
        return repository.save(entity);
    }
}
