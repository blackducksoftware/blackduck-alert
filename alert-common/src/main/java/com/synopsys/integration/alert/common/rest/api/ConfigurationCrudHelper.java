/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.function.ThrowingSupplier;

public class ConfigurationCrudHelper {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationCrudHelper.class);
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;
    private final DescriptorKey descriptorKey;

    public ConfigurationCrudHelper(AuthorizationManager authorizationManager, ConfigContextEnum context, DescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.context = context;
        this.descriptorKey = descriptorKey;
    }

    public <T extends Obfuscated<T>> ActionResponse<T> getOne(Supplier<Optional<T>> modelSupplier) {
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> optionalResponse = modelSupplier.get().map(Obfuscated::obfuscate);

        if (optionalResponse.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return new ActionResponse<>(HttpStatus.OK, optionalResponse.get());
    }

    public <T extends AlertSerializableModel & Obfuscated<T>> ActionResponse<AlertPagedModel<T>> getPage(Supplier<AlertPagedModel<T>> modelSupplier) {
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        AlertPagedModel<T> pagedResponse = modelSupplier.get().transformContent(Obfuscated::obfuscate);

        if (pagedResponse.getCurrentPage() > pagedResponse.getTotalPages()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return new ActionResponse<>(HttpStatus.OK, pagedResponse);
    }

    public <T extends Obfuscated<T>> ActionResponse<T> create(Supplier<ValidationResponseModel> validator, ThrowingSupplier<T, Exception> modelCreator) {
        if (!authorizationManager.hasCreatePermission(context, descriptorKey)) {
            return ActionResponse.createForbiddenResponse();
        }

        ValidationResponseModel validationResponse = validator.get();
        if (validationResponse.hasErrors()) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponse.getMessage());
        }
        try {
            return new ActionResponse<>(HttpStatus.OK, modelCreator.get().obfuscate());
        } catch (Exception ex) {
            logger.error("Error creating config:", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", ex.getMessage()));
        }
    }

    public <T extends Obfuscated<T>> ActionResponse<T> update(Supplier<ValidationResponseModel> validator, BooleanSupplier existingModelSupplier, ThrowingSupplier<T, AlertConfigurationException> updateFunction) {
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
            return new ActionResponse<>(HttpStatus.OK, updateFunction.get().obfuscate());
        } catch (Exception ex) {
            logger.error("Error updating config:", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error updating config: %s", ex.getMessage()));
        }
    }

    public <T> ActionResponse<T> delete(BooleanSupplier existingModelSupplier, Procedure deleteFunction) {
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
