/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class GlobalFieldModelToConcreteConversionService {
    private final DescriptorMap descriptorMap;
    private final Map<DescriptorKey, GlobalFieldModelToConcreteSaveActions> conversionActionMap;

    @Autowired
    public GlobalFieldModelToConcreteConversionService(List<? extends GlobalFieldModelToConcreteSaveActions> conversionActions, DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
        this.conversionActionMap = conversionActions.stream()
            .collect(Collectors.toMap(GlobalFieldModelToConcreteSaveActions::getDescriptorKey, Function.identity()));
    }

    public void createConcreteModel(FieldModel fieldModel) {
        Optional<DescriptorKey> descriptor = descriptorMap.getDescriptorKey(fieldModel.getDescriptorName());
        if (descriptor.isEmpty() || !isGlobalConfig(fieldModel)) {
            // not a field model that can be converted.
            return;
        }

        GlobalFieldModelToConcreteSaveActions conversionAction = conversionActionMap.get(descriptor.get());
        if (null != conversionAction) {
            conversionAction.createConcreteModel(fieldModel);
        }

    }

    public void updateConcreteModel(FieldModel fieldModel) {
        Optional<DescriptorKey> descriptor = descriptorMap.getDescriptorKey(fieldModel.getDescriptorName());
        if (descriptor.isEmpty() || !isGlobalConfig(fieldModel)) {
            // not a field model that can be converted.
            return;
        }
        GlobalFieldModelToConcreteSaveActions conversionAction = conversionActionMap.get(descriptor.get());
        if (null != conversionAction) {
            conversionAction.updateConcreteModel(fieldModel);
        }
    }

    private boolean isGlobalConfig(FieldModel fieldModel) {
        ConfigContextEnum fieldModelContext = ConfigContextEnum.valueOf(fieldModel.getContext());
        return ConfigContextEnum.GLOBAL == fieldModelContext;
    }
}
