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

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractResourceActions<T> implements ResourceActions<T>, ValidateAction<T>, TestAction<T> {
    public static final String FORBIDDEN_MESSAGE = "User not authorized to perform the request";
    private static final String RESOURCE_IDENTIFIER_MISSING = "Resource identifier missing.";
    private DescriptorKey descriptorKey;
    private AuthorizationManager authorizationManager;
    private ConfigContextEnum context;

    public AbstractResourceActions(DescriptorKey descriptorKey, ConfigContextEnum context, AuthorizationManager authorizationManager) {
        this.descriptorKey = descriptorKey;
        this.context = context;
        // to do change the authorization manager to use the context enum and the descriptor key
        this.authorizationManager = authorizationManager;

    }

    protected abstract ActionResponse<T> createResource(T resource);

    protected abstract ActionResponse<T> deleteResource(Long id);

    protected abstract ActionResponse<List<T>> readAllResources();

    protected abstract ActionResponse<T> readResource(Long id);

    protected abstract ValidationActionResponse testResource(T resource);

    protected abstract ActionResponse<T> updateResource(Long id, T resource);

    protected abstract ValidationActionResponse validateResource(T resource);

    @Override
    public ActionResponse<T> create(T resource) {
        if (!authorizationManager.hasCreatePermission(context.name(), descriptorKey.getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createResource(resource);
    }

    @Override
    public ActionResponse<List<T>> getAll() {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        return readAllResources();
    }

    @Override
    public ActionResponse<T> getOne(Long id) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, RESOURCE_IDENTIFIER_MISSING);
        }
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        return readResource(id);
    }

    @Override
    public ActionResponse<T> update(Long id, T resource) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, RESOURCE_IDENTIFIER_MISSING);
        }
        if (!authorizationManager.hasWritePermission(context.name(), descriptorKey.getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateResource(id, resource);
    }

    @Override
    public ActionResponse<T> delete(Long id) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, RESOURCE_IDENTIFIER_MISSING);
        }
        if (!authorizationManager.hasDeletePermission(context.name(), descriptorKey.getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        return deleteResource(id);
    }

    @Override
    public ValidationActionResponse test(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            ValidationResponseModel emptyModel = new ValidationResponseModel();
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE, emptyModel);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return validationResponse;
        }
        return testResource(resource);
    }

    @Override
    public ValidationActionResponse validate(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            ValidationResponseModel emptyModel = new ValidationResponseModel();
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE, emptyModel);
        }
        return validateResource(resource);
    }
}
