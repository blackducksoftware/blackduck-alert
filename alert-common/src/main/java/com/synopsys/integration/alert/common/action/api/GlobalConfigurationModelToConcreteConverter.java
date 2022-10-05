/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

/**
 * @deprecated This class is required for converting an old ConfigurationModel into the new GlobalConfigModel classes. This is a temporary class that should be removed once we
 * remove unsupported REST endpoints.
 */
@Deprecated(forRemoval = true)
public abstract class GlobalConfigurationModelToConcreteConverter<T extends ConfigWithMetadata> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Optional<T> convertAndValidate(ConfigurationModel globalConfigurationModel) {
        Optional<T> configModel = convert(globalConfigurationModel);

        if (configModel.isEmpty()) {
            return Optional.empty();
        }

        ValidationResponseModel validationResponseModel = validate(configModel.get());
        if (validationResponseModel.hasErrors()) {
            logger.error("Converted field model validation failed: {}", validationResponseModel.getMessage());
            for (AlertFieldStatus errorStatus : validationResponseModel.getErrors().values()) {
                logger.error("Field: '{}' failed with the error: {}", errorStatus.getFieldName(), errorStatus.getFieldMessage());
            }
            return Optional.empty();
        }
        return configModel;
    }

    protected abstract Optional<T> convert(ConfigurationModel globalConfigurationModel);

    protected abstract ValidationResponseModel validate(T configModel);
}
