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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;

public abstract class EnvironmentVariableHandlerV3<T extends Obfuscated<T>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final Set<String> environmentVariableNames;

    public EnvironmentVariableHandlerV3(String name, Set<String> environmentVariableNames) {
        this.name = name;
        this.environmentVariableNames = environmentVariableNames;
    }

    public String getName() {
        return name;
    }

    public Set<String> getVariableNames() {
        return environmentVariableNames;
    }

    public boolean isConfigurationMissing() {
        return configurationMissingCheck();
    }

    public EnvironmentProcessingResult updateFromEnvironment() {
        boolean configurationMissing = configurationMissingCheck();
        if (configurationMissing) {
            T configurationModel = configureModel();
            ValidationResponseModel validationResponseModel = validateConfiguration(configurationModel);
            if (validationResponseModel.hasErrors()) {
                logger.error("Error inserting startup values: {}", validationResponseModel.getMessage());
                Map<String, AlertFieldStatus> errors = validationResponseModel.getErrors();
                for (Map.Entry<String, AlertFieldStatus> error : errors.entrySet()) {
                    AlertFieldStatus status = error.getValue();
                    logger.error("Field: '{}' failed with the error: {}", status.getFieldName(), status.getFieldMessage());
                }
                return EnvironmentProcessingResult.empty();
            }
            return updateConfiguration(configurationModel);
        }

        return EnvironmentProcessingResult.empty();
    }

    protected abstract Boolean configurationMissingCheck();

    protected abstract T configureModel();

    protected abstract ValidationResponseModel validateConfiguration(T configModel);

    protected abstract EnvironmentProcessingResult updateConfiguration(T configModel);

}
