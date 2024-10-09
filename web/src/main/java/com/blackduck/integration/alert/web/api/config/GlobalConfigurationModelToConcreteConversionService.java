/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.config;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.blackduck.integration.alert.common.descriptor.DescriptorMap;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.util.DataStructureUtils;

/**
 * @deprecated Once unsupported global endpoints are removed in 8.0.0 we will no longer need the conversion service to translate between the old and new actions.
 */
@Component
@Deprecated(forRemoval = true)
public class GlobalConfigurationModelToConcreteConversionService {
    private final DescriptorMap descriptorMap;
    private final Map<DescriptorKey, GlobalConfigurationModelToConcreteSaveActions> conversionActionMap;

    @Autowired
    public GlobalConfigurationModelToConcreteConversionService(List<GlobalConfigurationModelToConcreteSaveActions> conversionActions, DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
        this.conversionActionMap = DataStructureUtils.mapToValues(conversionActions, GlobalConfigurationModelToConcreteSaveActions::getDescriptorKey);
    }

    public void createDefaultConcreteModel(String descriptorName, ConfigurationModel configurationModel) {
        descriptorMap.getDescriptorKey(descriptorName)
            .filter(ignored -> ConfigContextEnum.GLOBAL == configurationModel.getDescriptorContext())
            .filter(conversionActionMap::containsKey)
            .map(conversionActionMap::get)
            .ifPresent(conversionAction -> conversionAction.createConcreteModel(configurationModel));

    }

    public void updateDefaultConcreteModel(String descriptorName, ConfigurationModel configurationModel) {
        descriptorMap.getDescriptorKey(descriptorName)
            .filter(ignored -> ConfigContextEnum.GLOBAL == configurationModel.getDescriptorContext())
            .filter(conversionActionMap::containsKey)
            .map(conversionActionMap::get)
            .ifPresent(conversionAction -> conversionAction.updateConcreteModel(configurationModel));
    }

    public void deleteDefaultConcreteModel(String descriptorName, ConfigurationModel configurationModel) {
        descriptorMap.getDescriptorKey(descriptorName)
            .filter(ignored -> ConfigContextEnum.GLOBAL == configurationModel.getDescriptorContext())
            .filter(conversionActionMap::containsKey)
            .map(conversionActionMap::get)
            .ifPresent(conversionAction -> conversionAction.deleteConcreteModel(configurationModel));
    }
}
