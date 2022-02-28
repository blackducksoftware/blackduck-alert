/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.environment;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
public class ProxySettingsEnvironmentHandlerFactory implements EnvironmentVariableHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String HANDLER_NAME = "Proxy Settings";
    public static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_COMPONENT_SETTINGS_SETTINGS_PROXY_";

    // fields in model
    public static final String PROXY_HOST_KEY = ENVIRONMENT_VARIABLE_PREFIX + "HOST";
    public static final String PROXY_PORT_KEY = ENVIRONMENT_VARIABLE_PREFIX + "PORT";
    public static final String PROXY_USERNAME_KEY = ENVIRONMENT_VARIABLE_PREFIX + "USERNAME";
    public static final String PROXY_PASSWORD_KEY = ENVIRONMENT_VARIABLE_PREFIX + "PASSWORD";
    public static final String PROXY_NON_PROXY_HOSTS_KEY = ENVIRONMENT_VARIABLE_PREFIX + "NON_PROXY_HOSTS";

    public static final Set<String> PROXY_CONFIGURATION_KEYSET = Set.of(
        PROXY_HOST_KEY, PROXY_PORT_KEY, PROXY_USERNAME_KEY, PROXY_PASSWORD_KEY, PROXY_NON_PROXY_HOSTS_KEY
    );

    private final SettingsProxyConfigAccessor configAccessor;
    private final EnvironmentVariableUtility environmentVariableUtility;
    private final SettingsProxyValidator validator;

    @Autowired
    public ProxySettingsEnvironmentHandlerFactory(SettingsProxyConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility, SettingsProxyValidator validator) {
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
        this.validator = validator;
    }

    @Override
    public EnvironmentVariableHandler build() {
        return new EnvironmentVariableHandler(HANDLER_NAME, PROXY_CONFIGURATION_KEYSET, this::isConfigurationMissing, this::updateFunction);
    }

    private Boolean isConfigurationMissing() {
        return configAccessor.getConfiguration().isEmpty();
    }

    private EnvironmentProcessingResult updateFunction() {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(PROXY_CONFIGURATION_KEYSET);
        SettingsProxyModel configModel = new SettingsProxyModel();
        configModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        configureProxySettings(configModel);

        //TODO: Refactor and remove duplicate code after 6.10.0 model package refactor
        ValidationResponseModel validationResponseModel = validator.validate(configModel);
        if (validationResponseModel.hasErrors()) {
            logger.error("Error inserting startup values: {}", validationResponseModel.getMessage());
            Map<String, AlertFieldStatus> errors = validationResponseModel.getErrors();
            for (Map.Entry<String, AlertFieldStatus> error : errors.entrySet()) {
                AlertFieldStatus status = error.getValue();
                logger.error("Field: '{}' failed with the error: {}", status.getFieldName(), status.getFieldMessage());
            }
            return EnvironmentProcessingResult.empty();
        }

        SettingsProxyModel obfuscatedModel = configModel.obfuscate();
        obfuscatedModel.getProxyHost().ifPresent(value -> builder.addVariableValue(PROXY_HOST_KEY, value));
        obfuscatedModel.getProxyPort().map(String::valueOf).ifPresent(value -> builder.addVariableValue(PROXY_PORT_KEY, value));
        obfuscatedModel.getProxyUsername().ifPresent(value -> builder.addVariableValue(PROXY_USERNAME_KEY, value));
        obfuscatedModel.getNonProxyHosts().map(String::valueOf).ifPresent(value -> builder.addVariableValue(PROXY_NON_PROXY_HOSTS_KEY, value));

        if (Boolean.TRUE.equals(obfuscatedModel.getIsProxyPasswordSet())) {
            builder.addVariableValue(PROXY_PASSWORD_KEY, AlertConstants.MASKED_VALUE);
        }

        EnvironmentProcessingResult result = builder.build();
        if (result.hasValues()) {
            try {
                configAccessor.createConfiguration(configModel);
            } catch (AlertConfigurationException ex) {
                logger.error("Error creating the configuration: {}", ex.getMessage());
            }
        }

        return result;
    }

    private void configureProxySettings(SettingsProxyModel configuration) {
        environmentVariableUtility.getEnvironmentValue(PROXY_HOST_KEY)
            .ifPresent(configuration::setProxyHost);

        environmentVariableUtility.getEnvironmentValue(PROXY_PORT_KEY)
            .filter(NumberUtils::isDigits)
            .map(NumberUtils::toInt)
            .ifPresent(configuration::setProxyPort);

        environmentVariableUtility.getEnvironmentValue(PROXY_USERNAME_KEY)
            .ifPresent(configuration::setProxyUsername);

        environmentVariableUtility.getEnvironmentValue(PROXY_PASSWORD_KEY)
            .ifPresent(configuration::setProxyPassword);

        environmentVariableUtility.getEnvironmentValue(PROXY_NON_PROXY_HOSTS_KEY)
            .map(nonProxyHosts -> StringUtils.split(nonProxyHosts, ","))
            .map(Arrays::asList)
            .ifPresent(configuration::setNonProxyHosts);
    }
}
