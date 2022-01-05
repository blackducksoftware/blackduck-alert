/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;

@Deprecated(forRemoval = true)
@Component
public class DescriptorProcessor {
    private final DescriptorMap descriptorMap;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final Map<String, ConfigurationAction> allConfigurationActions;

    @Autowired
    public DescriptorProcessor(DescriptorMap descriptorMap, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, List<ConfigurationAction> configurationActions) {
        this.descriptorMap = descriptorMap;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.allConfigurationActions = DataStructureUtils.mapToValues(configurationActions, action -> action.getDescriptorKey().getUniversalKey());
    }

    public Optional<FieldModelTestAction> retrieveTestAction(FieldModel fieldModel) {
        ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        return retrieveTestAction(fieldModel.getDescriptorName(), descriptorContext);
    }

    public Optional<FieldModelTestAction> retrieveTestAction(String descriptorName, ConfigContextEnum context) {
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getTestAction(context));
    }

    public Optional<Descriptor> retrieveDescriptor(String descriptorName) {
        return descriptorMap.getDescriptorKey(descriptorName).flatMap(descriptorMap::getDescriptor);
    }

    public Optional<ConfigurationModel> getSavedEntity(Long id) {
        if (null != id) {
            return configurationModelConfigurationAccessor.getConfigurationById(id);
        }
        return Optional.empty();
    }

    public Optional<ApiAction> retrieveApiAction(FieldModel fieldModel) {
        return retrieveApiAction(fieldModel.getDescriptorName(), fieldModel.getContext());
    }

    private Optional<ApiAction> retrieveApiAction(String descriptorName, String context) {
        ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getApiAction(descriptorContext));
    }

    private Optional<ConfigurationAction> retrieveConfigurationAction(String descriptorName) {
        return Optional.ofNullable(allConfigurationActions.get(descriptorName));
    }

}
