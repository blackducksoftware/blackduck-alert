/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class ConfigurationHelper {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationHelper.class);
    private final AuthorizationManager authorizationManager;

    @Autowired
    public ConfigurationHelper(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public <T> ActionResponse<T> getOne(Supplier<Optional<T>> modelSupplier, ConfigContextEnum context, DescriptorKey descriptorKey) {
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> optionalResponse = modelSupplier.get();

        if (optionalResponse.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return new ActionResponse<>(HttpStatus.OK, optionalResponse.get());
    }

    public <T> ActionResponse<T> create(Supplier<ValidationResponseModel> validator, Callable<T> createdModelSupplier, ConfigContextEnum context, DescriptorKey descriptorKey) {
        if (!authorizationManager.hasCreatePermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        ValidationResponseModel validationResponse = validator.get();
        if (validationResponse.hasErrors()) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponse.getMessage());
        }
        try {
            return new ActionResponse<>(HttpStatus.OK, createdModelSupplier.call());
        } catch (Exception ex) {
            logger.error("Error creating config: {}", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", ex.getMessage()));
        }
    }

    public <T> ActionResponse<T> update(Supplier<ValidationResponseModel> validator, BooleanSupplier existingModelSupplier, Callable<T> updateFunction, ConfigContextEnum context, DescriptorKey descriptorKey) {
        if (!authorizationManager.hasWritePermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        boolean configurationExists = existingModelSupplier.getAsBoolean();
        if (!configurationExists) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        ValidationResponseModel validationResponse = validator.get();
        if (validationResponse.hasErrors()) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponse.getMessage());
        }
        try {
            return new ActionResponse<>(HttpStatus.OK, updateFunction.call());
        } catch (Exception ex) {
            logger.error("Error updating config: {}", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error updating config: %s", ex.getMessage()));
        }
    }

    public <T> ActionResponse<T> delete(BooleanSupplier existingModelSupplier, Runnable deleteFunction, ConfigContextEnum context, DescriptorKey descriptorKey) {
        if (!authorizationManager.hasDeletePermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        boolean configurationExists = existingModelSupplier.getAsBoolean();
        if (!configurationExists) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }
        deleteFunction.run();
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }
}
