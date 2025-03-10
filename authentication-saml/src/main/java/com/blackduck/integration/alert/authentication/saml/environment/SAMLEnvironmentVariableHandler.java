/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.environment;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableHandler;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.blackduck.integration.alert.common.util.DateUtils;

@Component
public class SAMLEnvironmentVariableHandler extends EnvironmentVariableHandler<SAMLConfigModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String HANDLER_NAME = "SAML Settings";
    public static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_COMPONENT_AUTHENTICATION_SETTINGS_SAML_";

    // Fields in model
    public static final String SAML_ENABLED_KEY = ENVIRONMENT_VARIABLE_PREFIX + "ENABLED";
    public static final String SAML_FORCE_AUTH_KEY = ENVIRONMENT_VARIABLE_PREFIX + "FORCE_AUTH";
    public static final String SAML_METADATA_URL_KEY = ENVIRONMENT_VARIABLE_PREFIX + "METADATA_URL";

    public static final Set<String> SAML_CONFIGURATION_KEY_SET = Set.of(
        SAML_ENABLED_KEY, SAML_FORCE_AUTH_KEY, SAML_METADATA_URL_KEY
    );

    private final SAMLConfigAccessor configAccessor;
    private final SAMLConfigurationValidator validator;
    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public SAMLEnvironmentVariableHandler(
        SAMLConfigAccessor configAccessor,
        SAMLConfigurationValidator validator,
        EnvironmentVariableUtility environmentVariableUtility
    ) {
        super(HANDLER_NAME, SAML_CONFIGURATION_KEY_SET, environmentVariableUtility);
        this.configAccessor = configAccessor;
        this.validator = validator;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        return !configAccessor.doesConfigurationExist();
    }

    @Override
    protected SAMLConfigModel configureModel() {
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        SAMLConfigModel samlConfigModel = new SAMLConfigModel();

        configureSAMLConfigFromEnv(samlConfigModel);

        samlConfigModel.setCreatedAt(createdAt);
        samlConfigModel.setLastUpdated(createdAt);

        return samlConfigModel;
    }

    @Override
    protected ValidationResponseModel validateConfiguration(SAMLConfigModel configModel) {
        return validator.validate(configModel);
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(SAMLConfigModel obfuscatedConfigModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(SAML_CONFIGURATION_KEY_SET);

        builder.addVariableValue(SAML_ENABLED_KEY, String.valueOf(obfuscatedConfigModel.getEnabled()));
        builder.addVariableValue(SAML_FORCE_AUTH_KEY, String.valueOf(obfuscatedConfigModel.getForceAuth()));
        obfuscatedConfigModel.getMetadataUrl()
            .ifPresent(value -> builder.addVariableValue(SAML_METADATA_URL_KEY, value));

        return builder.build();
    }

    @Override
    protected void saveConfiguration(SAMLConfigModel configModel, EnvironmentProcessingResult processingResult) {
        try {
            configAccessor.createConfiguration(configModel);
        } catch (AlertConfigurationException ex) {
            logger.error("Error creating the configuration: {}", ex.getMessage());
        }
    }

    private void configureSAMLConfigFromEnv(SAMLConfigModel configuration) {
        environmentVariableUtility.getEnvironmentValue(SAML_ENABLED_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configuration::setEnabled);
        environmentVariableUtility.getEnvironmentValue(SAML_FORCE_AUTH_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configuration::setForceAuth);
        environmentVariableUtility.getEnvironmentValue(SAML_METADATA_URL_KEY)
            .ifPresent(configuration::setMetadataUrl);
    }
}
