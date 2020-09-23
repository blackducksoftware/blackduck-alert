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

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractResourceActions<T> implements LongIdResourceActions<T>, ValidateAction<T>, TestAction<T> {

    private final DescriptorKey descriptorKey;
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;

    public AbstractResourceActions(DescriptorKey descriptorKey, ConfigContextEnum context, AuthorizationManager authorizationManager) {
        this.descriptorKey = descriptorKey;
        this.context = context;
        // to do change the authorization manager to use the context enum and the descriptor key
        this.authorizationManager = authorizationManager;

    }

    protected abstract ActionResponse<T> createWithoutChecks(T resource);

    protected abstract ActionResponse<T> deleteWithoutChecks(Long id);

    protected abstract ActionResponse<List<T>> readAllWithoutChecks();

    protected abstract ActionResponse<T> readWithoutChecks(Long id);

    protected abstract ValidationActionResponse testWithoutChecks(T resource);

    protected abstract ActionResponse<T> updateWithoutChecks(Long id, T resource);

    protected abstract ValidationActionResponse validateWithoutChecks(T resource);

    protected abstract Optional<T> findExisting(Long id);

    @Override
    public final ActionResponse<T> create(T resource) {
        if (!authorizationManager.hasCreatePermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createWithoutChecks(resource);
    }

    @Override
    public final ActionResponse<List<T>> getAll() {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }
        return readAllWithoutChecks();
    }

    @Override
    public final ActionResponse<T> getOne(Long id) {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return readWithoutChecks(id);
    }

    @Override
    public final ActionResponse<T> update(Long id, T resource) {
        if (!authorizationManager.hasWritePermission(context.name(), descriptorKey.getUniversalKey())) {
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

    @Override
    public final ActionResponse<T> delete(Long id) {
        if (!authorizationManager.hasDeletePermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return deleteWithoutChecks(id);
    }

    @Override
    public final ValidationActionResponse test(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            ValidationResponseModel responseModel = ValidationResponseModel.genericError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return ValidationActionResponse.createOKResponseWithContent(validationResponse);
        }
        ValidationActionResponse response = testWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }

    @Override
    public final ValidationActionResponse validate(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            ValidationResponseModel responseModel = ValidationResponseModel.genericError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse response = validateWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }
}
