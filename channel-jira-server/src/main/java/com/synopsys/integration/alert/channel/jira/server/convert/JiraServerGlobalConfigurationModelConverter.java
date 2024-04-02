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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

/**
 * @deprecated This class is used to support conversion between FieldModels and GlobalConfigurationModels. When FieldModels are deprecated in 8.0.0
 * this class will no longer be necessary.
 */
@Component
@Deprecated(forRemoval = true)
public class JiraServerGlobalConfigurationModelConverter extends GlobalConfigurationModelToConcreteConverter<JiraServerGlobalConfigModel> {
    public static final String URL_KEY = "jira.server.url";
    public static final String USERNAME_KEY = "jira.server.username";
    public static final String PASSWORD_KEY = "jira.server.password";
    public static final String DISABLE_PLUGIN_CHECK_KEY = "jira.server.disable.plugin.check";

    private final JiraServerGlobalConfigurationValidator validator;

    @Autowired
    public JiraServerGlobalConfigurationModelConverter(JiraServerGlobalConfigurationValidator validator) {
        this.validator = validator;
    }

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

        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            url,
            JiraServerAuthorizationMethod.BASIC
        );
        model.setUserName(username);
        model.setPassword(password);

        globalConfigurationModel.getField(DISABLE_PLUGIN_CHECK_KEY)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .map(Boolean::valueOf)
            .ifPresent(model::setDisablePluginCheck);

        return Optional.of(model);
    }

    @Override
    protected ValidationResponseModel validate(JiraServerGlobalConfigModel configModel, @Nullable String existingConfigurationId) {
        return validator.validate(configModel, existingConfigurationId);
    }
}
