/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;

public class EnvironmentVariableHandler2<T extends Obfuscated<T>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final Set<String> environmentVariableNames;
    private final BooleanSupplier configurationMissingCheck;
    private final Function<T, EnvironmentProcessingResult> updateFunction;
    private final Function<T, ValidationResponseModel> validator;
    private final Supplier<T> configModelSupplier;

    public static <T extends Obfuscated<T>> EnvironmentVariableHandler2<T> create(
        String name,
        Set<String> environmentVariableNames,
        BooleanSupplier configurationMissingCheck,
        Function<T, EnvironmentProcessingResult> updateFunction,
        Function<T, ValidationResponseModel> validator,
        Supplier<T> configModelSupplier
    ) {
        return new EnvironmentVariableHandler2<>(name, environmentVariableNames, configurationMissingCheck, updateFunction, validator, configModelSupplier);
    }

    /*
    public EnvironmentVariableHandler(String name, Set<String> environmentVariableNames, BooleanSupplier configurationMissingCheck, Supplier<EnvironmentProcessingResult> updateFunction) {
        this.name = name;
        this.environmentVariableNames = environmentVariableNames;
        this.configurationMissingCheck = configurationMissingCheck;
        this.updateFunction = updateFunction;
        //TODO:
        this.validator = null;
    }*/

    //TODO: Make this private, note that it may break unit tests
    public EnvironmentVariableHandler2(
        String name,
        Set<String> environmentVariableNames,
        BooleanSupplier configurationMissingCheck,
        Function<T, EnvironmentProcessingResult> updateFunction,
        Function<T, ValidationResponseModel> validator,
        Supplier<T> configModelSupplier
    ) {
        this.name = name;
        this.environmentVariableNames = environmentVariableNames;
        this.configurationMissingCheck = configurationMissingCheck;
        this.updateFunction = updateFunction;
        this.validator = validator;
        this.configModelSupplier = configModelSupplier;
    }

    public String getName() {
        return name;
    }

    public Set<String> getVariableNames() {
        return environmentVariableNames;
    }

    public boolean isConfigurationMissing() {
        return configurationMissingCheck.getAsBoolean();
    }

    public EnvironmentProcessingResult updateFromEnvironment() {
        boolean configurationMissing = configurationMissingCheck.getAsBoolean();
        if (configurationMissing) {
            //return updateFunction.apply();
        }

        return EnvironmentProcessingResult.empty();
    }

    //TODO: Rename and replace with the above
    public EnvironmentProcessingResult updateFromEnvironment2() {
        boolean configurationMissing = configurationMissingCheck.getAsBoolean();
        if (configurationMissing) {
            //T configurationModel = createConfiguration();
            T configurationModel = configModelSupplier.get();
            ValidationResponseModel validationResponseModel = validator.apply(configurationModel);
            if (validationResponseModel.hasErrors()) {
                logger.error("Error inserting startup values: {}", validationResponseModel.getMessage());
                Map<String, AlertFieldStatus> errors = validationResponseModel.getErrors();
                for (Map.Entry<String, AlertFieldStatus> error : errors.entrySet()) {
                    AlertFieldStatus status = error.getValue();
                    logger.error("Field: '{}' failed with the error: {}", status.getFieldName(), status.getFieldMessage());
                }
                return EnvironmentProcessingResult.empty();
            }
            return updateFunction.apply(configurationModel);
        }

        return EnvironmentProcessingResult.empty();
    }

    //Option 2, call methods from EnvVarProc
    //first call isConfigurationMissing -> if true, call get ConfigModel
    //public <T extends Obfuscated<T>> ValidationResponseModel configureModel(Supplier<T> modelCreator) {

    //}
}
