/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.convert;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalFieldModelSaveActions implements GlobalFieldModelToConcreteSaveActions {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EmailGlobalFieldModelConverter emailFieldModelConverter;
    private final EmailGlobalConfigAccessor configurationAccessor;

    @Autowired
    public EmailGlobalFieldModelSaveActions(EmailGlobalFieldModelConverter emailFieldModelConverter, EmailGlobalConfigAccessor configurationAccessor) {
        this.emailFieldModelConverter = emailFieldModelConverter;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return ChannelKeys.EMAIL;
    }

    @Override
    public void updateConcreteModel(FieldModel fieldModel) {
        Optional<UUID> defaultConfigurationId = configurationAccessor.getConfigurationByName(ConfigurationAccessor.DEFAULT_CONFIGURATION_NAME)
            .map(EmailGlobalConfigModel::getId)
            .map(UUID::fromString);
        Optional<EmailGlobalConfigModel> emailGlobalConfigModel = emailFieldModelConverter.convert(fieldModel);
        if (defaultConfigurationId.isPresent() && emailGlobalConfigModel.isPresent()) {
            EmailGlobalConfigModel model = emailGlobalConfigModel.get();
            try {
                configurationAccessor.updateConfiguration(defaultConfigurationId.get(), model);
            } catch (AlertConfigurationException ex) {
                logger.error("Error updating default email configuration from field model", ex);
            }
        }
    }

    @Override
    public void createConcreteModel(FieldModel fieldModel) {
        Optional<EmailGlobalConfigModel> emailGlobalConfigModel = emailFieldModelConverter.convert(fieldModel);
        if (emailGlobalConfigModel.isPresent()) {
            EmailGlobalConfigModel model = emailGlobalConfigModel.get();
            model.setName(ConfigurationAccessor.DEFAULT_CONFIGURATION_NAME);
            configurationAccessor.createConfiguration(model);
        }
    }
}
