/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.channel.email.web.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

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

    public ActionResponse<EmailGlobalConfigModel> getOne(Long id) {
        return configurationHelper.getOne(
            () -> configurationAccessor.getConfiguration(id).map(DatabaseModelWrapper::getModel)
        );
    }

    public ActionResponse<EmailGlobalConfigModel> create(EmailGlobalConfigModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            () -> configurationAccessor.createConfiguration(resource).getModel()
        );
    }

    public ActionResponse<EmailGlobalConfigModel> update(Long id, EmailGlobalConfigModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.updateConfiguration(id, requestResource).getModel()
        );
    }

    public ActionResponse<EmailGlobalConfigModel> delete(Long id) {
        return configurationHelper.delete(
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.deleteConfiguration(id)
        );
    }
}
