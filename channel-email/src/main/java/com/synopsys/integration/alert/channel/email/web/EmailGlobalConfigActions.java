/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.action.EmailGlobalTestAction;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationHelper;
import com.synopsys.integration.alert.common.rest.api.TestHelper;
import com.synopsys.integration.alert.common.rest.api.ValidationHelper;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigActions {
    private final ConfigurationHelper configurationHelper;
    private final ValidationHelper validationHelper;
    private final TestHelper testHelper;
    private final EmailGlobalConfigAccessor configurationAccessor;
    private final EmailGlobalConfigurationValidator validator;
    private final EmailGlobalTestAction testAction;

    @Autowired
    public EmailGlobalConfigActions(ConfigurationHelper configurationHelper, ValidationHelper validationHelper, TestHelper testHelper, EmailGlobalConfigAccessor configurationAccessor,
        EmailGlobalConfigurationValidator validator, EmailGlobalTestAction testAction) {
        this.configurationHelper = configurationHelper;
        this.validationHelper = validationHelper;
        this.testHelper = testHelper;
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
        this.testAction = testAction;
    }

    public ActionResponse<EmailGlobalConfigModel> getOne(Long id) {
        return configurationHelper.getOne(() -> configurationAccessor.getConfiguration(id).map(DatabaseModelWrapper::getModel), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ActionResponse<EmailGlobalConfigModel> create(EmailGlobalConfigModel resource) {
        return configurationHelper.create(() -> validator.validate(resource), () -> configurationAccessor.createConfiguration(resource).getModel(), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ActionResponse<EmailGlobalConfigModel> update(Long id, EmailGlobalConfigModel requestResource) {
        return configurationHelper.update(() -> validator.validate(requestResource), () -> configurationAccessor.getConfiguration(id).isPresent(), () -> configurationAccessor.updateConfiguration(id, requestResource).getModel(),
            ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ActionResponse<ValidationResponseModel> validate(EmailGlobalConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ActionResponse<EmailGlobalConfigModel> delete(Long id) {
        return configurationHelper.delete(() -> configurationAccessor.getConfiguration(id).isPresent(), () -> configurationAccessor.deleteConfiguration(id), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ActionResponse<ValidationResponseModel> test(String testAddress, EmailGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
        return testHelper.test(validationSupplier, () -> testAction.testConfig(testAddress, requestResource), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }
}
