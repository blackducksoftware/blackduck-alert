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

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractJobResourceActions implements JobResourceActions, ValidateAction<JobFieldModel>, TestAction<JobFieldModel> {
    private static final EnumSet<DescriptorType> ALLOWED_JOB_DESCRIPTOR_TYPES = EnumSet.of(DescriptorType.PROVIDER, DescriptorType.CHANNEL);
    private AuthorizationManager authorizationManager;
    private DescriptorAccessor descriptorAccessor;

    public AbstractJobResourceActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor) {
        this.authorizationManager = authorizationManager;
        this.descriptorAccessor = descriptorAccessor;
        this.descriptorAccessor = descriptorAccessor;
    }

    protected abstract Optional<JobFieldModel> findJobFieldModel(UUID id);

    protected abstract ActionResponse<JobFieldModel> createResource(JobFieldModel resource);

    protected abstract ActionResponse<JobFieldModel> deleteResource(UUID id);

    protected abstract ActionResponse<List<JobFieldModel>> readAllResources();

    protected abstract ValidationActionResponse testResource(JobFieldModel resource);

    protected abstract ActionResponse<JobFieldModel> updateResource(UUID id, JobFieldModel resource);

    protected abstract ValidationActionResponse validateResource(JobFieldModel resource);

    @Override
    public ActionResponse<JobFieldModel> create(JobFieldModel resource) {
        boolean hasPermissions = hasRequiredPermissions(resource.getFieldModels(), authorizationManager::hasCreatePermission);
        if (!hasPermissions) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createResource(resource);
    }

    @Override
    public ActionResponse<List<JobFieldModel>> getAll() {
        try {
            Set<String> descriptorNames = descriptorAccessor.getRegisteredDescriptors()
                                              .stream()
                                              .filter(descriptor -> ALLOWED_JOB_DESCRIPTOR_TYPES.contains(descriptor.getType()))
                                              .map(RegisteredDescriptorModel::getName)
                                              .collect(Collectors.toSet());
            if (!authorizationManager.anyReadPermission(List.of(ConfigContextEnum.DISTRIBUTION.name()), descriptorNames)) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
            }
            List<JobFieldModel> models = new LinkedList<>();
            ActionResponse<List<JobFieldModel>> response = readAllResources();
            List<JobFieldModel> allModels = response.getContent().orElse(List.of());
            for (JobFieldModel jobModel : allModels) {
                boolean includeJob = hasRequiredPermissions(jobModel.getFieldModels(), authorizationManager::hasReadPermission);
                if (includeJob) {
                    models.add(jobModel);
                }
            }
            return new ActionResponse<>(HttpStatus.OK, models);
        } catch (AlertException ex) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error reading job configurations: %s", ex.getMessage()));
        }

    }

    @Override
    public ActionResponse<JobFieldModel> getOne(UUID id) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, AbstractResourceActions.RESOURCE_IDENTIFIER_MISSING);
        }

        Optional<JobFieldModel> optionalModel = findJobFieldModel(id);

        if (optionalModel.isPresent()) {
            JobFieldModel fieldModel = optionalModel.get();
            boolean hasPermissions = hasRequiredPermissions(fieldModel.getFieldModels(), authorizationManager::hasReadPermission);
            if (!hasPermissions) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
            }
            return new ActionResponse<>(HttpStatus.OK, fieldModel);
        }

        return new ActionResponse<>(HttpStatus.NOT_FOUND, null);
    }

    @Override
    public ActionResponse<JobFieldModel> update(UUID id, JobFieldModel resource) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, AbstractResourceActions.RESOURCE_IDENTIFIER_MISSING);
        }
        boolean hasPermissions = hasRequiredPermissions(resource.getFieldModels(), authorizationManager::hasWritePermission);
        if (!hasPermissions) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
        }
        ValidationActionResponse validationResponse = validateResource(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateResource(id, resource);
    }

    @Override
    public ActionResponse<JobFieldModel> delete(UUID id) {
        if (null == id) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, AbstractResourceActions.RESOURCE_IDENTIFIER_MISSING);
        }
        Optional<JobFieldModel> optionalModel = findJobFieldModel(id);

        if (optionalModel.isPresent()) {
            JobFieldModel jobFieldModel = optionalModel.get();
            boolean hasPermissions = hasRequiredPermissions(jobFieldModel.getFieldModels(), authorizationManager::hasDeletePermission);
            if (!hasPermissions) {
                return new ActionResponse<>(HttpStatus.FORBIDDEN, AbstractResourceActions.FORBIDDEN_MESSAGE);
            }
        } else {
            return new ActionResponse<>(HttpStatus.NOT_FOUND, null);
        }
        return deleteResource(id);
    }

    @Override
    public ValidationActionResponse test(JobFieldModel resource) {
        boolean hasPermissions = hasRequiredPermissions(resource.getFieldModels(), authorizationManager::hasExecutePermission);
        if (!hasPermissions) {
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
    public ValidationActionResponse validate(JobFieldModel resource) {
        boolean hasPermissions = resource.getFieldModels()
                                     .stream()
                                     .allMatch(model ->
                                                   authorizationManager.hasCreatePermission(model.getContext(), model.getDescriptorName())
                                                       || authorizationManager.hasWritePermission(model.getContext(), model.getDescriptorName())
                                                       || authorizationManager.hasExecutePermission(model.getContext(), model.getDescriptorName()));
        if (!hasPermissions) {
            ValidationResponseModel responseModel = ValidationResponseModel.withoutFieldStatuses(AbstractResourceActions.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        return validateResource(resource);
    }

    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    public DescriptorAccessor getDescriptorAccessor() {
        return descriptorAccessor;
    }

    private boolean hasRequiredPermissions(Collection<FieldModel> fieldModels, BiFunction<String, String, Boolean> permissionChecker) {
        return fieldModels
                   .stream()
                   .allMatch(model -> permissionChecker.apply(model.getContext(), model.getDescriptorName()));
    }
}
