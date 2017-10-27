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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;

public abstract class ConfigController<D extends DatabaseEntity, R extends ConfigRestModel> {
    protected final Class<D> databaseEntityClass;
    protected final Class<R> configRestModelClass;
    protected final JpaRepository<D, Long> repository;
    protected final ObjectTransformer objectTransformer;

    public ConfigController(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final JpaRepository<D, Long> repository, final ObjectTransformer objectTransformer) {
        this.databaseEntityClass = databaseEntityClass;
        this.configRestModelClass = configRestModelClass;
        this.repository = repository;
        this.objectTransformer = objectTransformer;
    }

    public List<R> getConfig(final Long id) throws IntegrationException {
        if (id != null) {
            final D foundEntity = repository.findOne(id);
            if (foundEntity != null) {
                final R restModel = objectTransformer.tranformObject(foundEntity, configRestModelClass);
                if (restModel != null) {
                    return Arrays.asList(restModel);
                }
            }
            return Collections.emptyList();
        }
        final List<R> restModels = objectTransformer.tranformObjects(repository.findAll(), configRestModelClass);
        if (restModels != null) {
            return restModels;
        }
        return Collections.emptyList();
    }

    public ResponseEntity<String> postConfig(final R restModel) throws IntegrationException {
        final Long id = objectTransformer.stringToLong(restModel.getId());
        if (id == null || !repository.exists(id)) {
            ResponseEntity<String> response = validateConfig(restModel);
            if (response == null) {
                D createdEntity = objectTransformer.tranformObject(restModel, databaseEntityClass);
                if (createdEntity != null) {
                    createdEntity = repository.save(createdEntity);
                    response = createResponse(HttpStatus.CREATED, createdEntity.getId(), "Created.");
                } else {
                    response = createResponse(HttpStatus.BAD_REQUEST, null, "Could not create configuration from " + restModel.toString());
                }
            }
            return response;
        }
        return createResponse(HttpStatus.CONFLICT, id, "Provided id must not be in use. To update an existing configuration, use PUT.");
    }

    public ResponseEntity<String> putConfig(final R restModel) throws IntegrationException {
        final Long id = objectTransformer.stringToLong(restModel.getId());
        if (id != null && repository.exists(id)) {
            final D modelEntity = objectTransformer.tranformObject(restModel, databaseEntityClass);
            modelEntity.setId(id);
            ResponseEntity<String> response = validateConfig(restModel);
            if (response == null) {
                final D updatedEntity = repository.save(modelEntity);
                response = createResponse(HttpStatus.ACCEPTED, updatedEntity.getId(), "Updated.");
            }
            return response;
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No configuration with the specified id.");
    }

    public abstract ResponseEntity<String> validateConfig(R restModel);

    public ResponseEntity<String> deleteConfig(final R restModel) {
        final Long id = objectTransformer.stringToLong(restModel.getId());
        if (id != null && repository.exists(id)) {
            repository.delete(id);
            return createResponse(HttpStatus.ACCEPTED, id, "Deleted.");
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No configuration with the specified id.");
    }

    public abstract ResponseEntity<String> testConfig(final R restModel) throws IntegrationException;

    protected ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

}
