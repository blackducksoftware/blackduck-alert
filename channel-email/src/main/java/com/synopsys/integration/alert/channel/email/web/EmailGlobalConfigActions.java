/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.Optional;
import java.util.Set;

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
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigActions {
    private final Logger logger = LoggerFactory.getLogger(EmailGlobalConfigActions.class);
    private final AuthorizationManager authorizationManager;
    private final ConfigurationAccessor configurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final EmailGlobalConfigurationValidator validator;
    private final EmailGlobalTestAction testAction;

    @Autowired
    public EmailGlobalConfigActions(AuthorizationManager authorizationManager, ConfigurationAccessor configurationAccessor, ConfigurationFieldModelConverter modelConverter, EmailGlobalConfigurationValidator validator, EmailGlobalTestAction testAction) {
        this.authorizationManager = authorizationManager;
        this.configurationAccessor = configurationAccessor;
        this.modelConverter = modelConverter;
        this.validator = validator;
        this.testAction = testAction;
    }

    public ActionResponse<EmailGlobalConfigModel> getOne(Long id) {
        Optional<EmailGlobalConfigModel> optionalResponse = getEmailGlobalConfigResponse(id);

        if (optionalResponse.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL)) {
            return ActionResponse.createForbiddenResponse();
        }

        return new ActionResponse<>(HttpStatus.OK, optionalResponse.get());
    }

    private Optional<EmailGlobalConfigModel> getEmailGlobalConfigResponse(Long id) {
        return null;
    }

    public ActionResponse<EmailGlobalConfigModel> create(EmailGlobalConfigModel resource) {
        if (!authorizationManager.hasCreatePermission(ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL)) {
            return ActionResponse.createForbiddenResponse();
        }

        ValidationActionResponse validationResponse = validateWithoutChecks(resource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }

        return createWithoutChecks(resource);
    }

    public ActionResponse<EmailGlobalConfigModel> createWithoutChecks(EmailGlobalConfigModel requestResource) {
        // TODO: Implement with persistence changes
//        FieldModel requestAsFieldModel = null; // concreteModelTransformer.toFieldModel(requestResource);
//        Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelConverter.convertToConfigurationFieldModelMap(requestAsFieldModel);
//        ConfigurationModel configuration = fieldModelConfigurationAccessor.createConfiguration(ChannelKeys.EMAIL, ConfigContextEnum.GLOBAL, configurationFieldModelMap.values());
//        EmailGlobalConfigModel responseResource = concreteModelTransformer.fromConfigurationModel(configuration);
//        return new ActionResponse<>(HttpStatus.OK, responseResource);
        return null;
    }

    public ActionResponse<EmailGlobalConfigModel> update(Long id, EmailGlobalConfigModel requestResource) {
        if (!authorizationManager.hasWritePermission(ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL)) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<ConfigurationModel> existingModel = configurationAccessor.getConfigurationById(id);
        if (existingModel.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        ValidationActionResponse validationResponse = validateWithoutChecks(requestResource);
        if (validationResponse.isError()) {
            return new ActionResponse<>(validationResponse.getHttpStatus(), validationResponse.getMessage().orElse(null));
        }
        return updateWithoutChecks(id, requestResource);
    }

    public ActionResponse<EmailGlobalConfigModel> updateWithoutChecks(Long id, EmailGlobalConfigModel requestResource) {
        // TODO: implement with persistence changes
//        try {
//            FieldModel resourceAsFieldModel = concreteModelTransformer.toFieldModel(requestResource);
//
//            fieldModelConfigurationAccessor.getConfigurationById(id)
//                .map(modelConverter::convertToFieldModel)
//                .ifPresent(resourceAsFieldModel::fill);
//
//            Collection<ConfigurationFieldModel> updatedFields = modelConverter.convertToConfigurationFieldModelMap(resourceAsFieldModel).values();
//            ConfigurationModel configurationModel = fieldModelConfigurationAccessor.updateConfiguration(id, updatedFields);
//            EmailGlobalConfigModel updatedResponse = concreteModelTransformer.fromConfigurationModel(configurationModel);
//            return new ActionResponse<>(HttpStatus.OK, updatedResponse);
//        } catch (AlertException ex) {
//            logger.error("Error creating configuration", ex);
//            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", ex.getMessage()));
//        }
        return null;
    }

    public ActionResponse<ValidationResponseModel> validate(EmailGlobalConfigModel requestResource) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL)) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }

        return ValidationActionResponse.createOKResponseWithContent(validateWithoutChecks(requestResource));
    }

    public ValidationActionResponse validateWithoutChecks(EmailGlobalConfigModel requestResource) {
        Set<AlertFieldStatus> fieldStatuses = validator.validate(requestResource);
        if (fieldStatuses.isEmpty()) {
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success());
        } else {
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, ValidationResponseModel.fromStatusCollection(fieldStatuses));
        }
    }

    public ActionResponse<EmailGlobalConfigModel> delete(Long id) {
        if (!authorizationManager.hasDeletePermission(ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL)) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<ConfigurationModel> existingModel = configurationAccessor.getConfigurationById(id);
        if (existingModel.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return deleteWithoutChecks(id);
    }

    public ActionResponse<EmailGlobalConfigModel> deleteWithoutChecks(Long id) {
        configurationAccessor.getConfigurationById(id)
                         .map(modelConverter::convertToFieldModel)
                         .map(FieldModel::getId)
                         .map(Long::parseLong)
                         .ifPresent(configurationAccessor::deleteConfiguration);

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
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
        return testWithoutChecks(testAddress, requestResource);
    }

    public ActionResponse<ValidationResponseModel> testWithoutChecks(String testAddress, EmailGlobalConfigModel requestResource) {
        try {
            MessageResult messageResult = testAction.testConfig(testAddress, requestResource);
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success(messageResult.getStatusMessage()));
        } catch (AlertException e) {
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.generalError(e.getMessage()));
        }
    }

}
