/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.convert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalFieldModelSaveActions implements GlobalFieldModelToConcreteSaveActions {

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
        if (configurationAccessor.getConfigurationCount() == 1) {
            EmailGlobalConfigModel emailGlobalConfigModel = emailFieldModelConverter.convert(fieldModel);
            //configurationAccessor.updateConfiguration(emailGlobalConfigModel);
        }
    }

    @Override
    public void createConcreteModel(FieldModel fieldModel) {

    }
}
