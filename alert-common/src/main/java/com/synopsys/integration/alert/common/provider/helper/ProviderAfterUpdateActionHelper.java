/**
 * blackduck-alert
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
package com.synopsys.integration.alert.common.provider.helper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class ProviderAfterUpdateActionHelper {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurationAccessor configurationAccessor;
    private final List<ProviderKey> providerKeys;

    @Autowired
    public ProviderAfterUpdateActionHelper(ConfigurationAccessor configurationAccessor, List<ProviderKey> providerKeys) {
        this.configurationAccessor = configurationAccessor;
        this.providerKeys = providerKeys;
    }

    public void updateDistributionJobsWithNewProviderName(FieldModel previousProviderFields, FieldModel currentProviderFields) {
        Optional<String> optionalPreviousConfigName = previousProviderFields.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        String currentProviderConfigName = currentProviderFields.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                                               .orElseThrow(() -> new IllegalStateException("No provider config name present when the only possible valid state for this object would require it"));
        if (optionalPreviousConfigName.isPresent()) {
            List<ConfigurationModel> configurationModels = retrieveConfigurationModelsForOldConfigName(optionalPreviousConfigName.get(), previousProviderFields);
            updateStaleConfigurationModelsWithNewConfigName(configurationModels, currentProviderConfigName);
        } else {
            logger.debug("No previous provider config was present for config '{}', so no distribution jobs need to be updated", currentProviderConfigName);
        }
    }

    private List<ConfigurationModel> retrieveConfigurationModelsForOldConfigName(String previousProviderConfigName, FieldModel previousFieldModel) {
        Optional<ProviderKey> optionalProviderKey = lookUpProviderKey(previousFieldModel);
        if (optionalProviderKey.isPresent()) {
            try {
                return configurationAccessor.getConfigurationsByDescriptorKeyAndContext(optionalProviderKey.get(), ConfigContextEnum.DISTRIBUTION)
                           .stream()
                           .filter(configModel -> hasProviderConfigName(configModel, previousProviderConfigName))
                           .collect(Collectors.toList());
            } catch (AlertDatabaseConstraintException e) {
                logger.error("Failed to retrieve provider configurations from the database", e);
            }
        }
        return List.of();
    }

    private Optional<ProviderKey> lookUpProviderKey(FieldModel fieldModel) {
        return providerKeys
                   .stream()
                   .filter(key -> key.getUniversalKey().equals(fieldModel.getDescriptorName()))
                   .findAny();
    }

    private boolean hasProviderConfigName(ConfigurationModel configurationModel, String providerConfigName) {
        ConfigurationFieldModel foundConfigFieldModel = configurationModel.getCopyOfKeyToFieldMap()
                                                            .get(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        if (null != foundConfigFieldModel) {
            return foundConfigFieldModel.getFieldValue()
                       .filter(providerConfigName::equals)
                       .isPresent();
        }
        return false;
    }

    private void updateStaleConfigurationModelsWithNewConfigName(List<ConfigurationModel> staleConfigurationModels, String newProviderConfigName) {
        ConfigurationFieldModel newProviderConfigField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        newProviderConfigField.setFieldValue(newProviderConfigName);
        for (ConfigurationModel staleConfig : staleConfigurationModels) {
            Map<String, ConfigurationFieldModel> configFieldMap = staleConfig.getCopyOfKeyToFieldMap();
            configFieldMap.replace(newProviderConfigField.getFieldKey(), newProviderConfigField);
            try {
                configurationAccessor.updateConfiguration(staleConfig.getConfigurationId(), configFieldMap.values());
            } catch (AlertDatabaseConstraintException e) {
                logger.error("Could not update configuration with id '{}' to provider config name '{}'", staleConfig.getConfigurationId(), newProviderConfigName);
                logger.debug("Configuration update failed", e);
            }
        }
    }

}
