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

public abstract class AbstractResourceActions<T> implements LongResourceActions<T>, ValidateAction<T>, TestAction<T> {

    private DescriptorKey descriptorKey;
    private AuthorizationManager authorizationManager;
    private ConfigContextEnum context;

    public AbstractResourceActions(DescriptorKey descriptorKey, ConfigContextEnum context, AuthorizationManager authorizationManager) {
        this.descriptorKey = descriptorKey;
        this.context = context;
        // to do change the authorization manager to use the context enum and the descriptor key
        this.authorizationManager = authorizationManager;

    }

    protected abstract ActionResponse<T> createAfterChecks(T resource);

    protected abstract ActionResponse<T> deleteAfterChecks(Long id);

    protected abstract ActionResponse<List<T>> readAllAfterChecks();

    protected abstract ActionResponse<T> readAfterChecks(Long id);

    protected abstract ValidationActionResponse testAfterChecks(T resource);

    protected abstract ActionResponse<T> updateAfterChecks(Long id, T resource);

    protected abstract ValidationActionResponse validateAfterChecks(T resource);

    protected abstract Optional<T> findExisting(Long id);

    @Override
    public ActionResponse<T> create(T resource) {
        if (!authorizationManager.hasCreatePermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }
        ValidationActionResponse validationResponse = validateAfterChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createAfterChecks(resource);
    }

    @Override
    public ActionResponse<List<T>> getAll() {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }
        return readAllAfterChecks();
    }

    @Override
    public ActionResponse<T> getOne(Long id) {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return readAfterChecks(id);
    }

    @Override
    public ActionResponse<T> update(Long id, T resource) {
        if (!authorizationManager.hasWritePermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        ValidationActionResponse validationResponse = validateAfterChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateAfterChecks(id, resource);
    }

    @Override
    public ActionResponse<T> delete(Long id) {
        if (!authorizationManager.hasDeletePermission(context.name(), descriptorKey.getUniversalKey())) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> existingItem = findExisting(id);
        if (existingItem.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return deleteAfterChecks(id);
    }

    @Override
    public ValidationActionResponse test(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            ValidationResponseModel responseModel = ValidationResponseModel.withoutFieldStatuses(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateAfterChecks(resource);
        if (validationResponse.isError()) {
            return validationResponse;
        }
        return testAfterChecks(resource);
    }

    @Override
    public ValidationActionResponse validate(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            ValidationResponseModel responseModel = ValidationResponseModel.withoutFieldStatuses(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        return validateAfterChecks(resource);
    }
}
