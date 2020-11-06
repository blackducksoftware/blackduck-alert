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
package com.synopsys.integration.alert.channel.azure.boards.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class AzureBoardsGlobalApiAction extends ApiAction {
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;
    private final ConfigurationFieldModelConverter fieldModelConverter;

    @Autowired
    public AzureBoardsGlobalApiAction(ConfigurationAccessor configurationAccessor, DescriptorMap descriptorMap, ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
        this.fieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    public FieldModel beforeSaveAction(FieldModel fieldModel) throws AlertException {
        FieldModel updatedFieldModel = super.beforeSaveAction(fieldModel);
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(fieldModel.getDescriptorName());

        if (!descriptorKey.isPresent()) {
            return updatedFieldModel;
        }
        ConfigContextEnum context = ConfigContextEnum.valueOf(fieldModel.getContext());
        List<ConfigurationModel> existingConfig = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey.get(), context);
        Optional<ConfigurationModel> configurationModel = existingConfig.stream()
                                                              .findFirst();
        return configurationModel
                   .map((config) -> updateTokenFields(updatedFieldModel, config))
                   .orElse(updatedFieldModel);

    }

    @Override
    public FieldModel beforeUpdateAction(FieldModel fieldModel) throws AlertException {
        FieldModel updatedFieldModel = super.beforeUpdateAction(fieldModel);
        Optional<ConfigurationModel> existingConfig = configurationAccessor.getConfigurationById(Long.valueOf(fieldModel.getId()));
        return existingConfig
                   .map((config) -> updateTokenFields(updatedFieldModel, config))
                   .orElse(updatedFieldModel);
    }

    private FieldModel updateTokenFields(FieldModel fieldModel, ConfigurationModel configurationModel) {
        Map<String, FieldValueModel> keyToValues = new HashMap<>(fieldModel.getKeyToValues());
        Map<String, FieldValueModel> existingFields = fieldModelConverter.convertToFieldValuesMap(configurationModel.getCopyOfFieldList());

        // These fields are saved in the OAuth callback controller so we need to preserve their values on a save or an update.
        updateMapWithMissingField(AzureBoardsDescriptor.KEY_ACCESS_TOKEN, existingFields, keyToValues);
        updateMapWithMissingField(AzureBoardsDescriptor.KEY_REFRESH_TOKEN, existingFields, keyToValues);
        updateMapWithMissingField(AzureBoardsDescriptor.KEY_TOKEN_EXPIRATION_MILLIS, existingFields, keyToValues);
        updateMapWithMissingField(AzureBoardsDescriptor.KEY_OAUTH_USER_EMAIL, existingFields, keyToValues);

        return new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), keyToValues);
    }

    private void updateMapWithMissingField(String key, Map<String, FieldValueModel> databaseFields, Map<String, FieldValueModel> fieldModelFields) {
        if (databaseFields.containsKey(key)) {
            fieldModelFields.put(key, databaseFields.get(key));
        }
    }

}
