/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.api.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractConfigResourceActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class ConfigActions extends AbstractConfigResourceActions {
    private final Logger logger = LoggerFactory.getLogger(ConfigActions.class);
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final DescriptorMap descriptorMap;
    private final PKIXErrorResponseFactory pkixErrorResponseFactory;

    @Autowired
    public ConfigActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor, ConfigurationAccessor configurationAccessor,
        FieldModelProcessor fieldModelProcessor, DescriptorProcessor descriptorProcessor, ConfigurationFieldModelConverter modelConverter,
        DescriptorMap descriptorMap, PKIXErrorResponseFactory pkixErrorResponseFactory) {
        super(authorizationManager, descriptorAccessor);
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
        this.descriptorMap = descriptorMap;
        this.pkixErrorResponseFactory = pkixErrorResponseFactory;
    }

    @Override
    protected ActionResponse<MultiFieldModel> readAllWithoutChecks() {
        return new ActionResponse<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    protected ActionResponse<MultiFieldModel> readAllByContextAndDescriptorWithoutChecks(String context, String descriptorName) {
        ConfigContextEnum configContext = ConfigContextEnum.valueOf(context);
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(descriptorName);
        if (!descriptorKey.isPresent()) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, String.format("Unknown descriptor: %s", descriptorName));
        }
        try {
            List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey.get(), configContext);
            List<FieldModel> fieldModels = convertConfigurationModelList(descriptorName, context, configurationModels);
            return new ActionResponse<>(HttpStatus.OK, new MultiFieldModel(fieldModels));
        } catch (AlertDatabaseConstraintException ex) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error reading configurations: %s", ex.getMessage()));
        }
    }

    private List<FieldModel> convertConfigurationModelList(String descriptorName, String context, List<ConfigurationModel> configurationModels) {
        List<FieldModel> responseFieldModels = new LinkedList<>();
        List<FieldModel> fieldModelList = new LinkedList<>();
        if (null != configurationModels) {
            for (ConfigurationModel configurationModel : configurationModels) {
                try {
                    FieldModel fieldModel = modelConverter.convertToFieldModel(configurationModel);
                    fieldModelList.add(fieldModel);
                } catch (AlertDatabaseConstraintException ex) {
                    logger.error("Error converting to field model", ex);
                }
            }
        }
        if (fieldModelList.isEmpty()) {
            fieldModelList.add(new FieldModel(descriptorName, context, new HashMap<>()));
        }
        for (FieldModel fieldModel : fieldModelList) {
            try {
                responseFieldModels.add(fieldModelProcessor.performAfterReadAction(fieldModel));
            } catch (AlertException ex) {
                logger.error("Error performing after read action", ex);
            }
        }

        return responseFieldModels;
    }

    @Override
    protected Optional<FieldModel> findFieldModel(Long id) {
        Optional<FieldModel> optionalModel = Optional.empty();
        try {
            Optional<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationById(id);
            if (configurationModel.isPresent()) {
                FieldModel configurationFieldModel = modelConverter.convertToFieldModel(configurationModel.get());
                FieldModel fieldModel = fieldModelProcessor.performAfterReadAction(configurationFieldModel);
                optionalModel = Optional.of(fieldModel);
            }
        } catch (AlertException ex) {
            logger.error(String.format("Error finding configuration for id: %d", id), ex);
        }
        return optionalModel;
    }

    @Override
    protected ActionResponse<FieldModel> deleteWithoutChecks(Long id) {
        try {
            Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (configuration.isPresent()) {
                ConfigurationModel configurationModel = configuration.get();
                FieldModel convertedFieldModel = modelConverter.convertToFieldModel(configurationModel);
                FieldModel fieldModel = fieldModelProcessor.performBeforeDeleteAction(convertedFieldModel);
                configurationAccessor.deleteConfiguration(Long.parseLong(fieldModel.getId()));
                fieldModelProcessor.performAfterDeleteAction(fieldModel);
            }
        } catch (AlertException ex) {
            logger.error(String.format("Error deleting config id: %d", id), ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    @Override
    protected ActionResponse<FieldModel> createWithoutChecks(FieldModel resource) {
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(resource.getDescriptorName());
        if (descriptorKey.isPresent()) {
            try {
                FieldModel modifiedFieldModel = fieldModelProcessor.performBeforeSaveAction(resource);
                String context = modifiedFieldModel.getContext();
                Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelConverter.convertToConfigurationFieldModelMap(modifiedFieldModel);
                ConfigurationModel configuration = configurationAccessor.createConfiguration(descriptorKey.get(), EnumUtils.getEnum(ConfigContextEnum.class, context), configurationFieldModelMap.values());
                FieldModel dbSavedModel = modelConverter.convertToFieldModel(configuration);
                FieldModel afterSaveAction = fieldModelProcessor.performAfterSaveAction(dbSavedModel);
                FieldModel responseModel = dbSavedModel.fill(afterSaveAction);
                return new ActionResponse<>(HttpStatus.OK, responseModel);
            } catch (AlertException ex) {
                logger.error("Error creating configuration", ex);
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", ex.getMessage()));
            }
        }
        return new ActionResponse<>(HttpStatus.BAD_REQUEST, "descriptorName is missing or invalid");
    }

    @Override
    protected ActionResponse<FieldModel> updateWithoutChecks(Long id, FieldModel resource) {
        try {
            Optional<ConfigurationModel> optionalPreviousConfig = configurationAccessor.getConfigurationById(id);
            FieldModel previousFieldModel = optionalPreviousConfig.isPresent() ? modelConverter.convertToFieldModel(optionalPreviousConfig.get()) : null;

            FieldModel updatedFieldModel = fieldModelProcessor.performBeforeUpdateAction(resource);
            Collection<ConfigurationFieldModel> updatedFields = fieldModelProcessor.fillFieldModelWithExistingData(id, updatedFieldModel);
            ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, updatedFields);
            FieldModel dbSavedModel = modelConverter.convertToFieldModel(configurationModel);
            FieldModel afterUpdateAction = fieldModelProcessor.performAfterUpdateAction(previousFieldModel, dbSavedModel);
            FieldModel responseModel = dbSavedModel.fill(afterUpdateAction);
            return new ActionResponse<>(HttpStatus.OK, responseModel);
        } catch (AlertException ex) {
            logger.error("Error creating configuration", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", ex.getMessage()));
        }
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(FieldModel resource) {
        List<AlertFieldStatus> fieldStatuses = fieldModelProcessor.validateFieldModel(resource);
        ValidationResponseModel responseModel;
        HttpStatus status = HttpStatus.OK;
        if (fieldStatuses.isEmpty()) {
            responseModel = ValidationResponseModel.success("The configuration is valid");
        } else {
            status = HttpStatus.BAD_REQUEST;
            responseModel = ValidationResponseModel.fromStatusCollection("There were problems with the configuration", fieldStatuses);
        }
        return new ValidationActionResponse(status, responseModel);
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(FieldModel resource) {
        Optional<TestAction> testActionOptional = descriptorProcessor.retrieveTestAction(resource);
        ValidationResponseModel responseModel;
        String id = resource.getId();
        if (testActionOptional.isPresent()) {
            try {
                FieldModel upToDateFieldModel = fieldModelProcessor.createCustomMessageFieldModel(resource);
                FieldUtility fieldUtility = modelConverter.convertToFieldAccessor(upToDateFieldModel);
                TestAction testAction = testActionOptional.get();

                // TODO return the message from the result of testAction.testConfig(...)
                testAction.testConfig(upToDateFieldModel.getId(), upToDateFieldModel, fieldUtility);
                responseModel = ValidationResponseModel.success("Successfully sent test message.");
                return new ValidationActionResponse(HttpStatus.OK, responseModel);
            } catch (IntegrationRestException e) {
                logger.error(e.getMessage(), e);
                return ValidationActionResponse.createResponseFromIntegrationRestException(e);
            } catch (AlertFieldException e) {
                logger.error("Test Error with field Errors", e);
                responseModel = ValidationResponseModel.fromStatusCollection(e.getMessage(), e.getFieldErrors());
                return new ValidationActionResponse(HttpStatus.OK, responseModel);
            } catch (IntegrationException e) {
                // FIXME there are definitely other possibilities than this
                responseModel = pkixErrorResponseFactory.createSSLExceptionResponse(id, e)
                                    .orElse(ValidationResponseModel.generalError(e.getMessage()));
                return new ValidationActionResponse(HttpStatus.OK, responseModel);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                responseModel = pkixErrorResponseFactory.createSSLExceptionResponse(id, e)
                                    .orElse(ValidationResponseModel.generalError(e.getMessage()));
                return new ValidationActionResponse(HttpStatus.OK, responseModel);
            }
        }
        String descriptorName = resource.getDescriptorName();
        responseModel = ValidationResponseModel.generalError("Test functionality not implemented for " + descriptorName);
        return new ValidationActionResponse(HttpStatus.NOT_IMPLEMENTED, responseModel);
    }
}
