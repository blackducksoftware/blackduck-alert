/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.convert;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

/**
 * @deprecated This class is required for converting an old ConfigurationModel into the new GlobalConfigModel classes. This is a temporary class that should be removed once we
 * remove unsupported REST endpoints.
 */
@Component
@Deprecated(forRemoval = true)
public class ProxyConfigurationModelConverter extends GlobalConfigurationModelToConcreteConverter<SettingsProxyModel> {
    // TODO Remove field keys from ProxyManager when the old field models aren't used anymore.
    public static final String FIELD_KEY_USERNAME = "settings.proxy.username";
    public static final String FIELD_KEY_PASSWORD = "settings.proxy.password";
    public static final String FIELD_KEY_HOST = "settings.proxy.host";
    public static final String FIELD_KEY_PORT = "settings.proxy.port";
    public static final String FIELD_KEY_NON_PROXY_HOSTS = "settings.proxy.non.proxy.hosts";

    private final SettingsProxyValidator validator;

    @Autowired
    public ProxyConfigurationModelConverter(SettingsProxyValidator validator) {
        this.validator = validator;
    }

    @Override
    public Optional<SettingsProxyModel> convert(ConfigurationModel globalConfigurationModel) {
        String proxyHost = globalConfigurationModel.getField(FIELD_KEY_HOST)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);
        Integer proxyPort = globalConfigurationModel.getField(FIELD_KEY_PORT)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .filter(NumberUtils::isDigits)
            .map(NumberUtils::toInt)
            .orElse(null);

        if (proxyHost == null || proxyPort == null) {
            return Optional.empty();
        }

        SettingsProxyModel model = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, proxyHost, proxyPort);
        globalConfigurationModel.getField(FIELD_KEY_NON_PROXY_HOSTS)
            .map(ConfigurationFieldModel::getFieldValues)
            .map(ArrayList::new)
            .ifPresent(model::setNonProxyHosts);
        globalConfigurationModel.getField(FIELD_KEY_USERNAME)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .ifPresent(model::setProxyUsername);
        globalConfigurationModel.getField(FIELD_KEY_PASSWORD)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .ifPresent(model::setProxyPassword);

        return Optional.of(model);
    }

    @Override
    protected ValidationResponseModel validate(SettingsProxyModel configModel, String existingConfigurationId) {
        //Since there is only a single email global configuration, existingConfigurationId is ignored.
        return validator.validate(configModel);
    }
}
