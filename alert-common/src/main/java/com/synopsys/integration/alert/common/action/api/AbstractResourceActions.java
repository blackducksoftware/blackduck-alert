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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
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
        if (!authorizationManager.hasCreatePermission(context, descriptorKey)) {
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
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Get all"));
            return ActionResponse.createForbiddenResponse();
        }
        List<T> resources = retrieveDatabaseModels().stream()
                                .map(this::convertDatabaseModelToRestModel)
                                .collect(Collectors.toList());
        return new ActionResponse<>(HttpStatus.OK, createMultiResponseModel(resources));
    }

    public final ActionResponse<T> getOne(Long id) {
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
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
        if (!authorizationManager.hasWritePermission(context, descriptorKey)) {
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
        if (!authorizationManager.hasDeletePermission(context, descriptorKey)) {
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
        if (!authorizationManager.hasExecutePermission(context, descriptorKey)) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Test"));
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return ValidationActionResponse.createOKResponseWithContent(validationResponse);
        }
        return testWithoutChecks(resource);
    }

    public final ValidationActionResponse validate(T resource) {
        if (!authorizationManager.hasExecutePermission(context, descriptorKey)) {
            logger.debug(String.format(FORBIDDEN_ACTION_FORMAT, "Validate"));
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse response = validateWithoutChecks(resource);
        return ValidationActionResponse.createOKResponseWithContent(response);
    }

}
