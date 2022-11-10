/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.convert;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.action.JiraServerGlobalCrudActions;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

/**
 * @deprecated This class is required for converting an old ConfigurationModel into the new GlobalConfigModel classes. This is a temporary class that should be removed once we
 * remove unsupported REST endpoints in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class JiraServerGlobalConfigurationModelSaveActions implements GlobalConfigurationModelToConcreteSaveActions {
    private final JiraServerGlobalConfigurationModelConverter jiraFieldModelConverter;
    private final JiraServerGlobalCrudActions configurationActions;
    private final JiraServerGlobalConfigAccessor configurationAccessor;

    public JiraServerGlobalConfigurationModelSaveActions(
        JiraServerGlobalConfigurationModelConverter jiraFieldModelConverter, JiraServerGlobalCrudActions configurationActions,
        JiraServerGlobalConfigAccessor configurationAccessor
    ) {
        this.jiraFieldModelConverter = jiraFieldModelConverter;
        this.configurationActions = configurationActions;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return ChannelKeys.JIRA_SERVER;
    }

    @Override
    public void updateConcreteModel(ConfigurationModel configurationModel) {
        Optional<UUID> defaultConfigurationId = configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString);
        Optional<JiraServerGlobalConfigModel> jiraGlobalConfigModel = jiraFieldModelConverter.convertAndValidate(
            configurationModel,
            defaultConfigurationId.map(UUID::toString).orElse(null)
        );
        if (jiraGlobalConfigModel.isPresent()) {
            JiraServerGlobalConfigModel model = jiraGlobalConfigModel.get();
            model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
            if (defaultConfigurationId.isPresent()) {
                configurationActions.update(defaultConfigurationId.get(), model);
            } else {
                configurationActions.create(model);
            }
        }
    }

    @Override
    public void createConcreteModel(ConfigurationModel configurationModel) {
        Optional<JiraServerGlobalConfigModel> jiraGlobalConfigModel = jiraFieldModelConverter.convertAndValidate(configurationModel, null);
        if (jiraGlobalConfigModel.isPresent()) {
            JiraServerGlobalConfigModel model = jiraGlobalConfigModel.get();
            model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
            configurationActions.create(model);
        }
    }

    @Override
    public void deleteConcreteModel(ConfigurationModel configurationModel) {
        Optional<UUID> defaultConfigurationId = configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString);
        defaultConfigurationId.ifPresent(configurationActions::delete);
    }
}
