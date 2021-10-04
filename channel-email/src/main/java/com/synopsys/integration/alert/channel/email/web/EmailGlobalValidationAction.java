/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalValidationAction {
    private final EmailGlobalConfigurationValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public EmailGlobalValidationAction(EmailGlobalConfigurationValidator validator, ConfigurationValidationHelper validationHelper) {
        this.validator = validator;
        this.validationHelper = validationHelper;
    }

    public ActionResponse<ValidationResponseModel> validate(EmailGlobalConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

}
