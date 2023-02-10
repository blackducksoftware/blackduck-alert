/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;

public abstract class EnvironmentVariableHandler<T extends Obfuscated<T>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final Set<String> environmentVariableNames;
    private final EnvironmentVariableUtility environmentVariableUtility;

    protected EnvironmentVariableHandler(String name, Set<String> environmentVariableNames, EnvironmentVariableUtility environmentVariableUtility) {
        this.name = name;
        this.environmentVariableNames = environmentVariableNames;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    public String getName() {
        return name;
    }

    public Set<String> getVariableNames() {
        return environmentVariableNames;
    }

    private boolean doVariablesExistInEnvironment() {
        for (String variableName : environmentVariableNames) {
            if (environmentVariableUtility.getEnvironmentValue(variableName).isPresent()) {
                return true;
            }
        }
        logger.info("Did not find any environment variables configured for: {}", name);
        return false;
    }

    public EnvironmentProcessingResult updateFromEnvironment() {
        boolean configurationMissing = configurationMissingCheck();
        if (configurationMissing && doVariablesExistInEnvironment()) {
            T configurationModel = configureModel();
            ValidationResponseModel validationResponseModel = validateConfiguration(configurationModel);
            if (validationResponseModel.hasErrors()) {
                logger.error("Error inserting startup values: {}", validationResponseModel.getMessage());
                for (AlertFieldStatus errorStatus : validationResponseModel.getErrors().values()) {
                    logger.error("Field: '{}' failed with the error: {}", errorStatus.getFieldName(), errorStatus.getFieldMessage());
                }
                return EnvironmentProcessingResult.empty();
            }
            EnvironmentProcessingResult processingResult = buildProcessingResult(configurationModel.obfuscate());
            if (processingResult.hasValues()) {
                saveConfiguration(configurationModel, processingResult);
            }
            return processingResult;
        }

        return EnvironmentProcessingResult.empty();
    }

    protected abstract Boolean configurationMissingCheck();

    protected abstract T configureModel();

    protected abstract ValidationResponseModel validateConfiguration(T configModel);

    protected abstract EnvironmentProcessingResult buildProcessingResult(T obfuscatedConfigModel);

    protected abstract void saveConfiguration(T configModel, EnvironmentProcessingResult processingResult);

}
