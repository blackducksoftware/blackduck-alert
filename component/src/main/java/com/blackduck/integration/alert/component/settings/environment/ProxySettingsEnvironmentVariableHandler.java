/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.environment;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableHandler;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class ProxySettingsEnvironmentVariableHandler extends EnvironmentVariableHandler<SettingsProxyModel> {
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
    public ProxySettingsEnvironmentVariableHandler(
        SettingsProxyConfigAccessor configAccessor,
        EnvironmentVariableUtility environmentVariableUtility,
        SettingsProxyValidator validator
    ) {
        super(HANDLER_NAME, PROXY_CONFIGURATION_KEYSET, environmentVariableUtility);
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
        this.validator = validator;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        return !configAccessor.doesConfigurationExist();
    }

    @Override
    protected SettingsProxyModel configureModel() {
        String name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        String proxyHost = environmentVariableUtility.getEnvironmentValue(PROXY_HOST_KEY).orElse(null);
        Integer proxyPort = environmentVariableUtility.getEnvironmentValue(PROXY_PORT_KEY)
            .filter(NumberUtils::isDigits)
            .map(NumberUtils::toInt)
            .orElse(null);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        SettingsProxyModel configModel = new SettingsProxyModel(null, name, proxyHost, proxyPort);
        configModel.setCreatedAt(createdAt);
        configModel.setLastUpdated(createdAt);

        configureProxySettings(configModel);

        return configModel;
    }

    @Override
    protected ValidationResponseModel validateConfiguration(SettingsProxyModel configModel) {
        return validator.validate(configModel);
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(SettingsProxyModel obfuscatedConfigModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(PROXY_CONFIGURATION_KEYSET);

        if (StringUtils.isNotBlank(obfuscatedConfigModel.getProxyHost())) {
            builder.addVariableValue(PROXY_HOST_KEY, obfuscatedConfigModel.getProxyHost());
        }
        String proxyPort = String.valueOf(obfuscatedConfigModel.getProxyPort());
        if (StringUtils.isNotBlank(proxyPort)) {
            builder.addVariableValue(PROXY_PORT_KEY, proxyPort);
        }
        obfuscatedConfigModel.getProxyUsername().ifPresent(value -> builder.addVariableValue(PROXY_USERNAME_KEY, value));
        obfuscatedConfigModel.getNonProxyHosts().map(String::valueOf).ifPresent(value -> builder.addVariableValue(PROXY_NON_PROXY_HOSTS_KEY, value));

        if (Boolean.TRUE.equals(obfuscatedConfigModel.getIsProxyPasswordSet())) {
            builder.addVariableValue(PROXY_PASSWORD_KEY, AlertConstants.MASKED_VALUE);
        }

        return builder.build();
    }

    @Override
    protected void saveConfiguration(SettingsProxyModel configModel, EnvironmentProcessingResult processingResult) {
        try {
            configAccessor.createConfiguration(configModel);
        } catch (AlertConfigurationException ex) {
            logger.error("Error creating the configuration: {}", ex.getMessage());
        }
    }

    private void configureProxySettings(SettingsProxyModel configuration) {
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
