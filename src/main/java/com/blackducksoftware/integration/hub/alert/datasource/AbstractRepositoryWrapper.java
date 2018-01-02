/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.datasource.entity.BaseEntity;

public abstract class AbstractRepositoryWrapper<D extends BaseEntity, ID extends Serializable, R extends JpaRepository<D, ID>> {

    private final R repository;

    public AbstractRepositoryWrapper(final R repository) {
        this.repository = repository;
    }

    public R getRepository() {
        return repository;
    }

    public boolean exists(final ID id) {
        return getRepository().exists(id);
    }

    public void delete(final ID id) {
        getRepository().delete(id);
    }

    public D findOne(final ID id) {
        return getRepository().findOne(id);
    }

    public List<D> findAll() {
        return getRepository().findAll();
    }

    public D save(final D entity) {
        final D encryptedEntity = encryptSensitiveData(entity);
        return getRepository().save(encryptedEntity);
    }

    public abstract D encryptSensitiveData(D entity);
}
