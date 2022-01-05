/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.convert;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;

@Component
public class ProxyConfigurationModelConverter implements GlobalConfigurationModelToConcreteConverter<SettingsProxyModel> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    // TODO Remove field keys from ProxyManager when the old field models aren't used anymore.
    public static final String FIELD_KEY_USERNAME = "settings.proxy.username";
    public static final String FIELD_KEY_PASSWORD = "settings.proxy.password";
    public static final String FIELD_KEY_HOST = "settings.proxy.host";
    public static final String FIELD_KEY_PORT = "settings.proxy.port";
    public static final String FIELD_KEY_NON_PROXY_HOSTS = "settings.proxy.non.proxy.hosts";

    @Override
    public Optional<SettingsProxyModel> convert(ConfigurationModel globalConfigurationModel) {
        Optional<SettingsProxyModel> convertedModel = Optional.empty();
        SettingsProxyModel model = new SettingsProxyModel();
        try {
            globalConfigurationModel.getField(FIELD_KEY_HOST)
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .ifPresent(model::setProxyHost);
            globalConfigurationModel.getField(FIELD_KEY_PORT)
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .map(Integer::valueOf)
                .ifPresent(model::setProxyPort);
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
            convertedModel = Optional.of(model);
        } catch (NumberFormatException ex) {
            logger.error("Error converting field model to concrete proxy configuration", ex);
        }
        return convertedModel;
    }
}
