/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractConfigResourceActions implements ConfigResourceActions {
    private final AuthorizationManager authorizationManager;
    private final DescriptorAccessor descriptorAccessor;

    protected AbstractConfigResourceActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor) {
        this.authorizationManager = authorizationManager;
        this.descriptorAccessor = descriptorAccessor;
    }

    protected abstract ActionResponse<FieldModel> createWithoutChecks(FieldModel resource);

    protected abstract ActionResponse<FieldModel> deleteWithoutChecks(Long id);

    protected abstract ActionResponse<MultiFieldModel> readAllWithoutChecks();

    protected abstract ActionResponse<MultiFieldModel> readAllByContextAndDescriptorWithoutChecks(String context, String descriptorName);

    protected abstract Optional<FieldModel> findFieldModel(Long id);

    protected abstract ValidationActionResponse testWithoutChecks(FieldModel resource);

    protected abstract ActionResponse<FieldModel> updateWithoutChecks(Long id, FieldModel resource);

    protected abstract ValidationActionResponse validateWithoutChecks(FieldModel resource);

    @Override
    public final ActionResponse<MultiFieldModel> getAllByContextAndDescriptor(String context, String descriptorName) {
        if (!authorizationManager.hasReadPermission(context, descriptorName)) {
            return ActionResponse.createForbiddenResponse();
        }
        return readAllByContextAndDescriptorWithoutChecks(context, descriptorName);
    }

    @Override
    public final ActionResponse<FieldModel> create(FieldModel resource) {
        if (!authorizationManager.hasCreatePermission(resource.getContext(), resource.getDescriptorName())) {
            return ActionResponse.createForbiddenResponse();
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return createWithoutChecks(resource);
    }

    @Override
    public final ActionResponse<MultiFieldModel> getAll() {
        Set<String> descriptorNames = descriptorAccessor.getRegisteredDescriptors()
                                          .stream()
                                          .map(RegisteredDescriptorModel::getName)
                                          .collect(Collectors.toSet());
        if (!authorizationManager.anyReadPermission(List.of(ConfigContextEnum.DISTRIBUTION, ConfigContextEnum.GLOBAL), descriptorNames)) {
            return ActionResponse.createForbiddenResponse();
        }
        return readAllWithoutChecks();
    }

    @Override
    public final ActionResponse<FieldModel> getOne(Long id) {
        Optional<FieldModel> fieldModel = findFieldModel(id);
        if (fieldModel.isPresent()) {
            FieldModel model = fieldModel.get();
            if (!authorizationManager.hasReadPermission(model.getContext(), model.getDescriptorName())) {
                return ActionResponse.createForbiddenResponse();
            }

            return new ActionResponse<>(HttpStatus.OK, model);
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public final ActionResponse<FieldModel> update(Long id, FieldModel resource) {
        if (!authorizationManager.hasWritePermission(resource.getContext(), resource.getDescriptorName())) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<FieldModel> existingModel = findFieldModel(id);
        if (existingModel.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateWithoutChecks(id, resource);
    }

    @Override
    public final ActionResponse<FieldModel> delete(Long id) {
        Optional<FieldModel> fieldModel = findFieldModel(id);
        if (fieldModel.isPresent()) {
            FieldModel model = fieldModel.get();
            if (!authorizationManager.hasDeletePermission(model.getContext(), model.getDescriptorName())) {
                return ActionResponse.createForbiddenResponse();
            }
        }

        Optional<FieldModel> existingModel = findFieldModel(id);
        if (existingModel.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }
        return deleteWithoutChecks(id);
    }

    @Override
    public final ValidationActionResponse test(FieldModel resource) {
        if (!authorizationManager.hasExecutePermission(resource.getContext(), resource.getDescriptorName())) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return ValidationActionResponse.createOKResponseWithContent(validationResponse);
        }
        return testWithoutChecks(resource);
    }

    @Override
    public final ValidationActionResponse validate(FieldModel resource) {
        if (!authorizationManager.hasExecutePermission(resource.getContext(), resource.getDescriptorName())) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse response = validateWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }

    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    public DescriptorAccessor getDescriptorAccessor() {
        return descriptorAccessor;
    }

}
