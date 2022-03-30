/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.convert;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.action.EmailGlobalCrudActions;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigurationModelSaveActions implements GlobalConfigurationModelToConcreteSaveActions {
    private final EmailGlobalConfigurationModelConverter emailFieldModelConverter;
    private final EmailGlobalCrudActions configurationActions;
    private final EmailGlobalConfigAccessor configurationAccessor;

    @Autowired
    public EmailGlobalConfigurationModelSaveActions(
        EmailGlobalConfigurationModelConverter emailFieldModelConverter, EmailGlobalCrudActions configurationActions,
        EmailGlobalConfigAccessor configurationAccessor
    ) {
        this.emailFieldModelConverter = emailFieldModelConverter;
        this.configurationActions = configurationActions;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return ChannelKeys.EMAIL;
    }

    @Override
    public void updateConcreteModel(ConfigurationModel configurationModel) {
        Optional<UUID> defaultConfigurationId = configurationAccessor.getConfiguration()
            .map(EmailGlobalConfigModel::getId)
            .map(UUID::fromString);
        Optional<EmailGlobalConfigModel> emailGlobalConfigModel = emailFieldModelConverter.convertAndValidate(configurationModel);
        if (defaultConfigurationId.isPresent()) {
            emailGlobalConfigModel.ifPresent(configurationActions::update);
        }
    }

    @Override
    public void createConcreteModel(ConfigurationModel configurationModel) {
        Optional<EmailGlobalConfigModel> emailGlobalConfigModel = emailFieldModelConverter.convertAndValidate(configurationModel);
        emailGlobalConfigModel.ifPresent(configurationActions::create);
    }

    @Override
    public void deleteConcreteModel(ConfigurationModel configurationModel) {
        if (configurationAccessor.doesConfigurationExist()) {
            configurationAccessor.deleteConfiguration();
        }
    }
}
