/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.common.model.Obfuscated;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.function.ThrowingSupplier;

public class ConfigurationCrudHelper {
    private final Logger logger = AlertLoggerFactory.getLogger(getClass());
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;
    private final DescriptorKey descriptorKey;

    private static final String CRUD_ACTION_TEMPLATE = "CRUD action {}: ";
    private static final String ACTION_CALLED_TEMPLATE = CRUD_ACTION_TEMPLATE + "{} called.";
    private static final String ACTION_SUCCESS_TEMPLATE = CRUD_ACTION_TEMPLATE + "{} completed successfully.";
    private static final String ACTION_NOT_FOUND_TEMPLATE = CRUD_ACTION_TEMPLATE + "{} not found.";
    private static final String ACTION_BAD_REQUEST_TEMPLATE = CRUD_ACTION_TEMPLATE + "{} bad request.";
    private static final String ACTION_MISSING_PERMISSIONS_TEMPLATE = CRUD_ACTION_TEMPLATE + "{} missing permissions.";

    public ConfigurationCrudHelper(AuthorizationManager authorizationManager, ConfigContextEnum context, DescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.context = context;
        this.descriptorKey = descriptorKey;
    }

    public <T extends Obfuscated<T>> ActionResponse<T> getOne(Supplier<Optional<T>> modelSupplier) {
        String actionName = "getOne";
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> optionalResponse = modelSupplier.get().map(Obfuscated::obfuscate);

        if (optionalResponse.isEmpty()) {
            logger.trace(ACTION_NOT_FOUND_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        return new ActionResponse<>(HttpStatus.OK, optionalResponse.get());
    }

    public <T extends AlertSerializableModel & Obfuscated<T>> ActionResponse<AlertPagedModel<T>> getPage(Supplier<AlertPagedModel<T>> modelSupplier) {
        String actionName = "getPage";
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        if (!authorizationManager.hasReadPermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return ActionResponse.createForbiddenResponse();
        }

        AlertPagedModel<T> pagedResponse = modelSupplier.get().transformContent(Obfuscated::obfuscate);

        if (pagedResponse.getCurrentPage() > pagedResponse.getTotalPages()) {
            logger.trace(ACTION_NOT_FOUND_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        return new ActionResponse<>(HttpStatus.OK, pagedResponse);
    }

    public <T extends Obfuscated<T>> ActionResponse<T> create(Supplier<ValidationResponseModel> validator, BooleanSupplier existingModelSupplier, ThrowingSupplier<T, Exception> modelCreator) {
        String actionName = "create";
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        if (!authorizationManager.hasCreatePermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return ActionResponse.createForbiddenResponse();
        }

        ValidationResponseModel validationResponse = validator.get();
        if (validationResponse.hasErrors()) {
            logger.trace(ACTION_BAD_REQUEST_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponse.getMessage());
        }

        boolean configurationExists = existingModelSupplier.getAsBoolean();
        if (configurationExists) {
            logger.trace(ACTION_BAD_REQUEST_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, "A configuration with this name already exists.");
        }

        try {
            return new ActionResponse<>(HttpStatus.OK, modelCreator.get().obfuscate());
        } catch (AlertConfigurationException ex) {
            logger.trace(ACTION_BAD_REQUEST_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, String.format("Error creating config: %s", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Error creating config:", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", ex.getMessage()));
        } finally {
            logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        }
    }

    public <T extends Obfuscated<T>> ActionResponse<T> update(Supplier<ValidationResponseModel> validator, BooleanSupplier existingModelSupplier, ThrowingSupplier<T, AlertConfigurationException> updateFunction) {
        String actionName = "update";
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        if (!authorizationManager.hasWritePermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return ActionResponse.createForbiddenResponse();
        }

        boolean configurationExists = existingModelSupplier.getAsBoolean();
        if (!configurationExists) {
            logger.trace(ACTION_NOT_FOUND_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        ValidationResponseModel validationResponse = validator.get();
        if (validationResponse.hasErrors()) {
            logger.trace(ACTION_BAD_REQUEST_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResponse.getMessage());
        }
        try {
            return new ActionResponse<>(HttpStatus.OK, updateFunction.get().obfuscate());
        } catch (AlertConfigurationException ex) {
            logger.trace(ACTION_BAD_REQUEST_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, String.format("Error updating config: %s", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Error updating config:", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error updating config: %s", ex.getMessage()));
        } finally {
            logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        }
    }

    public <T> ActionResponse<T> delete(BooleanSupplier existingModelSupplier, Procedure deleteFunction) {
        String actionName = "delete";
        logger.trace(ACTION_CALLED_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        if (!authorizationManager.hasDeletePermission(context, descriptorKey)) {
            logger.trace(ACTION_MISSING_PERMISSIONS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return ActionResponse.createForbiddenResponse();
        }

        boolean configurationExists = existingModelSupplier.getAsBoolean();
        if (!configurationExists) {
            logger.trace(ACTION_NOT_FOUND_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }
        deleteFunction.run();
        logger.trace(ACTION_SUCCESS_TEMPLATE, descriptorKey.getUniversalKey(), actionName);
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }
}
