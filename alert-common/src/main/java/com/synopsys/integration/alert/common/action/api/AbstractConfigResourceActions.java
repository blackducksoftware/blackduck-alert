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
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractConfigResourceActions implements ResourceActions<FieldModel>, TestAction<FieldModel>, ValidateAction<FieldModel> {
    private AuthorizationManager authorizationManager;
    private DescriptorAccessor descriptorAccessor;

    public AbstractConfigResourceActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor) {
        this.authorizationManager = authorizationManager;
        this.descriptorAccessor = descriptorAccessor;
    }

    public AbstractConfigResourceActions(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    protected abstract ActionResponse<FieldModel> createResource(FieldModel resource);

    protected abstract ActionResponse<FieldModel> deleteResource(Long id);

    protected abstract ActionResponse<List<FieldModel>> readAllResources();

    protected abstract ActionResponse<List<FieldModel>> readAllByContextAndDescriptor(String context, String descriptorName);

    protected abstract Optional<FieldModel> findFieldModel(Long id);

    protected abstract ValidationActionResponse testResource(FieldModel resource);

    protected abstract ActionResponse<FieldModel> updateResource(Long id, FieldModel resource);

    protected abstract ValidationActionResponse validateResource(FieldModel resource);

    public ActionResponse<List<FieldModel>> getAllByContextAndDescriptor(String context, String descriptorName) {
        if (!authorizationManager.hasReadPermission(context, descriptorName)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        return readAllByContextAndDescriptor(context, descriptorName);
    }

    @Override
    public ActionResponse<FieldModel> create(FieldModel resource) {
        if (!authorizationManager.hasCreatePermission(resource.getContext(), resource.getDescriptorName())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createResource(resource);
    }

    @Override
    public ActionResponse<List<FieldModel>> getAll() {
        try {
            Set<String> descriptorNames = descriptorAccessor.getRegisteredDescriptors()
                                              .stream()
                                              .map(RegisteredDescriptorModel::getName)
                                              .collect(Collectors.toSet());
            if (!authorizationManager.anyReadPermission(List.of(ConfigContextEnum.DISTRIBUTION.name(), ConfigContextEnum.GLOBAL.name()), descriptorNames)) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
            }
            return readAllResources();
        } catch (AlertException ex) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error reading configurations: %s", ex.getMessage()));
        }
    }

    @Override
    public ActionResponse<FieldModel> getOne(Long id) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, AbstractResourceActions.RESOURCE_IDENTIFIER_MISSING);
        }
        Optional<FieldModel> fieldModel = findFieldModel(id);
        if (fieldModel.isPresent()) {
            FieldModel model = fieldModel.get();
            if (!authorizationManager.hasReadPermission(model.getContext(), model.getDescriptorName())) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
            }

            return new ActionResponse<>(HttpStatus.OK, model);
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND, null);
    }

    @Override
    public ActionResponse<FieldModel> update(Long id, FieldModel resource) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, AbstractResourceActions.RESOURCE_IDENTIFIER_MISSING);
        }
        if (!authorizationManager.hasWritePermission(resource.getContext(), resource.getDescriptorName())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateResource(id, resource);
    }

    @Override
    public ActionResponse<FieldModel> delete(Long id) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, AbstractResourceActions.RESOURCE_IDENTIFIER_MISSING);
        }
        Optional<FieldModel> fieldModel = findFieldModel(id);
        if (fieldModel.isPresent()) {
            FieldModel model = fieldModel.get();
            if (!authorizationManager.hasDeletePermission(model.getContext(), model.getDescriptorName())) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
            }
        }
        return deleteResource(id);
    }

    @Override
    public ValidationActionResponse test(FieldModel resource) {
        if (!authorizationManager.hasExecutePermission(resource.getContext(), resource.getDescriptorName())) {
            ValidationResponseModel responseModel = ValidationResponseModel.withoutFieldStatuses(AbstractResourceActions.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return validationResponse;
        }
        return testResource(resource);
    }

    @Override
    public ValidationActionResponse validate(FieldModel resource) {
        if (!authorizationManager.hasExecutePermission(resource.getContext(), resource.getDescriptorName())) {
            ValidationResponseModel responseModel = ValidationResponseModel.withoutFieldStatuses(AbstractResourceActions.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        return validateResource(resource);
    }
}
