/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.config;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
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
