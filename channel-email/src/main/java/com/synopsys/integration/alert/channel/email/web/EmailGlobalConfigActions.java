/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.action.EmailGlobalTestAction;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationHelper;
import com.synopsys.integration.alert.common.rest.api.ValidationHelper;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigActions {
    private final Logger logger = LoggerFactory.getLogger(EmailGlobalConfigActions.class);
    private final AuthorizationManager authorizationManager;
    private final ConfigurationHelper configurationHelper;
    private final ValidationHelper validationHelper;
    private final EmailGlobalConfigAccessor configurationAccessor;
    private final EmailGlobalConfigurationValidator validator;
    private final EmailGlobalTestAction testAction;

    @Autowired
    public EmailGlobalConfigActions(AuthorizationManager authorizationManager, ConfigurationHelper configurationHelper, ValidationHelper validationHelper, EmailGlobalConfigAccessor configurationAccessor,
        EmailGlobalConfigurationValidator validator, EmailGlobalTestAction testAction) {
        this.authorizationManager = authorizationManager;
        this.configurationHelper = configurationHelper;
        this.validationHelper = validationHelper;
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
        return configurationHelper.update(() -> validator.validate(requestResource),
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.updateConfiguration(id, requestResource).getModel(),
            ConfigContextEnum.GLOBAL,
            ChannelKeys.EMAIL);
    }

    public ActionResponse<ValidationResponseModel> validate(EmailGlobalConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ValidationActionResponse validateWithoutChecks(EmailGlobalConfigModel requestResource) {
        ValidationResponseModel validationResponse = validator.validate(requestResource);
        HttpStatus statusCode = validationResponse.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return new ValidationActionResponse(statusCode, validationResponse);
    }

    public ActionResponse<EmailGlobalConfigModel> delete(Long id) {
        return configurationHelper.delete(() -> configurationAccessor.getConfiguration(id).isPresent(), () -> configurationAccessor.deleteConfiguration(id), ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
    }

    public ActionResponse<ValidationResponseModel> test(String testAddress, EmailGlobalConfigModel requestResource) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL)) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }
        ValidationActionResponse validationResponse = validateWithoutChecks(requestResource);
        if (validationResponse.isError()) {
            return ValidationActionResponse.createOKResponseWithContent(validationResponse);
        }

        try {
            MessageResult messageResult = testAction.testConfig(testAddress, requestResource);
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success(messageResult.getStatusMessage()));
        } catch (AlertException e) {
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.generalError(e.getMessage()));
        }
    }

}
