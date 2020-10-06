/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.temp;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractConfigResourceActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class TempConfigActions extends AbstractConfigResourceActions {
    private final Logger logger = LoggerFactory.getLogger(TempConfigActions.class);
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final DescriptorMap descriptorMap;

    @Autowired
    public TempConfigActions(AuthorizationManager authorizationManager, DescriptorAccessor descriptorAccessor, ConfigurationAccessor configurationAccessor,
        FieldModelProcessor fieldModelProcessor, ConfigurationFieldModelConverter modelConverter, DescriptorMap descriptorMap) {
        super(authorizationManager, descriptorAccessor);
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.modelConverter = modelConverter;
        this.descriptorMap = descriptorMap;
    }

    @Override
    protected ActionResponse<MultiFieldModel> readAllWithoutChecks() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ActionResponse<MultiFieldModel> readAllByContextAndDescriptorWithoutChecks(String context, String descriptorName) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(FieldModel resource) {
        throw new UnsupportedOperationException();
    }

}
