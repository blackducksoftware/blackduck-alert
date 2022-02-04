/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.convert;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

@Component
public class JiraServerGlobalConfigurationModelConverter implements GlobalConfigurationModelToConcreteConverter<JiraServerGlobalConfigModel> {
    public static final String URL_KEY = "jira.server.url";
    public static final String USERNAME_KEY = "jira.server.username";
    public static final String PASSWORD_KEY = "jira.server.password";
    public static final String DISABLE_PLUGIN_CHECK_KEY = "jira.server.disable.plugin.check";

    @Override
    public Optional<JiraServerGlobalConfigModel> convert(ConfigurationModel globalConfigurationModel) {

        String url = globalConfigurationModel.getField(URL_KEY)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);

        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }

        String username = globalConfigurationModel.getField(USERNAME_KEY)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);

        String password = globalConfigurationModel.getField(PASSWORD_KEY)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);

        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, url, username, password);

        globalConfigurationModel.getField(DISABLE_PLUGIN_CHECK_KEY)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .map(Boolean::valueOf)
            .ifPresent(model::setDisablePluginCheck);

        return Optional.of(model);
    }
}
