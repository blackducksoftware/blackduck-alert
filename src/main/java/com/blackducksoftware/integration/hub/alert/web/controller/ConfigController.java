/**
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ChannelRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;

public abstract class ConfigController<D extends DatabaseEntity, R extends ChannelRestModel> {
    protected final JpaRepository<D, Long> repository;

    @Autowired
    public ConfigController(final JpaRepository<D, Long> repository) {
        this.repository = repository;
    }

    public List<R> getConfig(final Long id) {
        if (id != null) {
            final D foundEntity = repository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(databaseModelToRestModel(foundEntity));
            } else {
                return Collections.emptyList();
            }
        }
        return databaseModelsToRestModels(repository.findAll());
    }

    public ResponseEntity<String> postConfig(final R restModel) {
        if (restModel.getId() == null || !repository.exists(restModel.getId())) {
            final D createdEntity = repository.save(restModelToDatabaseModel(restModel));
            return createResponse(HttpStatus.CREATED, createdEntity.getId(), "Created.");
        }
        return createResponse(HttpStatus.CONFLICT, restModel.getId(), "Invalid id.");
    }

    public ResponseEntity<String> putConfig(final R restModel) {
        if (restModel.getId() != null && repository.exists(restModel.getId())) {
            final D updatedEntity = repository.save(restModelToDatabaseModel(restModel));
            return createResponse(HttpStatus.CREATED, updatedEntity.getId(), "Updated.");
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public ResponseEntity<String> deleteConfig(final R restModel) {
        if (restModel.getId() != null && repository.exists(restModel.getId())) {
            repository.delete(restModel.getId());
            return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "Deleted.");
        }
        return createResponse(HttpStatus.BAD_REQUEST, restModel.getId(), "No configuration with the specified id.");
    }

    public abstract ResponseEntity<String> testConfig(final R restModel);

    public abstract D restModelToDatabaseModel(final R restModel);

    public abstract R databaseModelToRestModel(final D databaseModel);

    public List<R> databaseModelsToRestModels(final List<D> databaseModels) {
        final List<R> restModels = new ArrayList<>();
        for (final D databaseModel : databaseModels) {
            restModels.add(databaseModelToRestModel(databaseModel));
        }
        return restModels;
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }
}
