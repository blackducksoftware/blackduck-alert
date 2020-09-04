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

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractResourceActions<T> implements ResourceActions<T>, ValidateAction<T>, TestAction<T> {
    private DescriptorKey descriptorKey;
    private AuthorizationManager authorizationManager;
    private ConfigContextEnum context;

    public AbstractResourceActions(DescriptorKey descriptorKey, ConfigContextEnum context, AuthorizationManager authorizationManager) {
        this.descriptorKey = descriptorKey;
        this.context = context;
        // to do change the authorization manager to use the context enum and the descriptor key
        this.authorizationManager = authorizationManager;

    }

    @Override
    public ActionResponse<T> create(T resource) {
        if (!authorizationManager.hasCreatePermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }

    @Override
    public ActionResponse<List<T>> getAll() {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }

    @Override
    public ActionResponse<T> getOne(Long id) {
        if (!authorizationManager.hasReadPermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }

    @Override
    public ActionResponse<T> update(Long id, T resource) {
        if (!authorizationManager.hasWritePermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }

    @Override
    public ActionResponse<T> delete(Long id) {
        if (!authorizationManager.hasDeletePermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }

    @Override
    public ValidationActionResponse testConfig(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }

    @Override
    public ValidationActionResponse validate(T resource) {
        if (!authorizationManager.hasExecutePermission(context.name(), descriptorKey.getUniversalKey())) {
            throw ResponseFactory.createForbiddenException();
        }
        return null;
    }
}
