/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public abstract class AbstractConfigurationUtility implements ConfigurationUtility {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DescriptorKey key;
    private ConfigContextEnum context;
    private ConfigurationAccessor configurationAccessor;
    private ApiAction apiAction;
    private ConfigurationFieldModelConverter configurationFieldModelConverter;

    public AbstractConfigurationUtility(DescriptorKey key, ConfigContextEnum context, ConfigurationAccessor configurationAccessor, ApiAction apiAction, ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.key = key;
        this.context = context;
        this.configurationAccessor = configurationAccessor;
        this.apiAction = apiAction;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    public DescriptorKey getKey() {
        return key;
    }

    @Override
    public boolean doesConfigurationExist() {
        try {
            return !configurationAccessor.getConfigurationByDescriptorKeyAndContext(key, context).isEmpty();
        } catch (AlertException ex) {
            logger.debug("Error reading configuration from database.", ex);
            return false;
        }
    }

    @Override
    public Optional<FieldModel> getFieldModel() throws AlertException {
        final Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getConfigurationByDescriptorKeyAndContext(key, context)
                                                                            .stream()
                                                                            .findFirst();

        if (configurationModelOptional.isPresent()) {
            ConfigurationModel configurationModel = configurationModelOptional.get();
            FieldModel fieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
            return Optional.ofNullable(apiAction.afterGetAction(fieldModel));
        }

        return Optional.empty();
    }

    @Override
    public Optional<ConfigurationModel> getConfiguration() throws AlertException {
        Optional<FieldModel> fieldModel = getFieldModel();

        if (fieldModel.isPresent()) {
            ConfigurationModel configurationModel = configurationFieldModelConverter.convertToConfigurationModel(fieldModel.get());
            return Optional.ofNullable(configurationModel);
        }

        return Optional.empty();
    }

    @Override
    public FieldModel saveSettings(final FieldModel fieldModel) throws AlertException {
        FieldModel beforeAction = apiAction.beforeSaveAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeAction).values();
        ConfigurationModel configuration = configurationAccessor.createConfiguration(key, context, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configuration);
        return apiAction.afterSaveAction(convertedFieldModel);
    }

    @Override
    public FieldModel updateSettings(final Long id, final FieldModel fieldModel) throws AlertException {
        FieldModel beforeUpdateAction = apiAction.beforeUpdateAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeUpdateAction).values();
        ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
        return apiAction.afterUpdateAction(convertedFieldModel);
    }
}
