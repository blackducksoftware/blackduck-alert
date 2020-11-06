/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.common.rest.model.MultiResponseModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class AbstractResourceActions<T extends Config, D extends AlertSerializableModel, M extends MultiResponseModel<T>> {
    private final DescriptorKey descriptorKey;
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;

    private final Logger logger = AlertLoggerFactory.getLogger(getClass());

    public static final String FORBIDDEN_ACTION_FORMAT = "%s action is forbidden. This user is not authorized to perform this action.";

    public AbstractResourceActions(DescriptorKey descriptorKey, ConfigContextEnum context, AuthorizationManager authorizationManager) {
        this.descriptorKey = descriptorKey;
        this.context = context;
        // to do change the authorization manager to use the context enum and the descriptor key
        this.authorizationManager = authorizationManager;
    }

    protected abstract ActionResponse<T> createWithoutChecks(T resource);

    protected abstract ActionResponse<T> deleteWithoutChecks(Long id);

    protected abstract List<D> retrieveDatabaseModels();

    protected abstract T convertDatabaseModelToRestModel(D databaseModel);

    protected abstract M createMultiResponseModel(List<T> resources);

    protected abstract ValidationActionResponse testWithoutChecks(T resource);

    protected abstract ActionResponse<T> updateWithoutChecks(Long id, T resource);

    protected abstract ValidationActionResponse validateWithoutChecks(T resource);

    protected abstract Optional<T> findExisting(Long id);

    public final ActionResponse<T> create(T resource) {
        if (!authorizationManager.hasCreatePermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Create"));
            return ActionResponse.createForbiddenResponse();
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createWithoutChecks(resource);
    }

    public final ActionResponse<M> getAll() {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Get all"));
            return ActionResponse.createForbiddenResponse();
        }
        List<T> resources = retrieveDatabaseModels().stream()
                                .map(this::convertDatabaseModelToRestModel)
                                .collect(Collectors.toList());
        return new ActionResponse<>(HttpStatus.OK, createMultiResponseModel(resources));
    }

    public final ActionResponse<T> getOne(Long id) {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Get one"));
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return new ActionResponse<>(HttpStatus.OK, existingItem.get());
    }

    public final ActionResponse<T> update(Long id, T resource) {
        if (!authorizationManager.hasWritePermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Update"));
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateWithoutChecks(id, resource);
    }

    public final ActionResponse<T> delete(Long id) {
        if (!authorizationManager.hasDeletePermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Delete"));
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return deleteWithoutChecks(id);
    }

    public final ValidationActionResponse test(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Test"));
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return ValidationActionResponse.createOKResponseWithContent(validationResponse);
        }
        ValidationActionResponse response = testWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }

    public final ValidationActionResponse validate(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Validate"));
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse response = validateWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }

}
