/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final EmailGlobalConfigAccessor configurationAccessor;
    private final EmailGlobalConfigurationValidator validator;

    @Autowired
    public EmailGlobalCrudActions(AuthorizationManager authorizationManager, EmailGlobalConfigAccessor configurationAccessor, EmailGlobalConfigurationValidator validator) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
    }

    public ActionResponse<EmailGlobalConfigModel> getOne() {
        return configurationHelper.getOne(configurationAccessor::getConfiguration);
    }

    public ActionResponse<EmailGlobalConfigModel> create(EmailGlobalConfigModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            () -> configurationAccessor.doesConfigurationExist(),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<EmailGlobalConfigModel> update(EmailGlobalConfigModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> configurationAccessor.doesConfigurationExist(),
            () -> configurationAccessor.updateConfiguration(requestResource)
        );
    }

    public ActionResponse<EmailGlobalConfigModel> delete() {
        return configurationHelper.delete(
            () -> configurationAccessor.doesConfigurationExist(),
            () -> configurationAccessor.deleteConfiguration()
        );
    }
}
